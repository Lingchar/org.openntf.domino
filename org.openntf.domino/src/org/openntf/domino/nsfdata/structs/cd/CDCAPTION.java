package org.openntf.domino.nsfdata.structs.cd;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import org.openntf.domino.nsfdata.structs.COLOR_VALUE;
import org.openntf.domino.nsfdata.structs.FONTID;
import org.openntf.domino.nsfdata.structs.SIG;

/**
 * This CD record defines the properties of a caption for a grapic [sic] record. The actual caption text follows the fixed part of the
 * record.
 * 
 * @since Lotus Notes/Domino 5.0
 *
 */
public class CDCAPTION extends CDRecord {
	public static enum Position {
		BELOW_CENTER((byte) 0), MIDDLE_CENTER((byte) 1);

		private final byte value_;

		private Position(final byte value) {
			value_ = value;
		}

		public byte getValue() {
			return value_;
		}

		public static Position valueOf(final byte typeCode) {
			for (Position type : values()) {
				if (type.getValue() == typeCode) {
					return type;
				}
			}
			throw new IllegalArgumentException("No matching Position found for type code " + typeCode);
		}
	}

	public CDCAPTION(final SIG signature, final ByteBuffer data) {
		super(signature, data);
	}

	/**
	 * @return Caption text length
	 */
	public int getCaptionLength() {
		return getData().getShort(getData().position() + 0) & 0xFFFF;
	}

	/**
	 * @return CAPTION_POSITION_xxx
	 */
	public Position getPosition() {
		return Position.valueOf(getData().get(getData().position() + 2));
	}

	public FONTID getFontId() {
		ByteBuffer data = getData().duplicate();
		data.position(data.position() + 3);
		data.limit(data.position() + FONTID.SIZE);
		return new FONTID(data);
	}

	public COLOR_VALUE getFontColor() {
		ByteBuffer data = getData().duplicate();
		data.position(data.position() + 7);
		data.limit(data.position() + 6);
		return new COLOR_VALUE(data);
	}

	public byte[] getReserved() {
		byte[] result = new byte[11];
		for (int i = 0; i < 11; i++) {
			result[i] = getData().get(getData().position() + 13 + i);
		}
		return result;
	}

	public String getCaption() {
		ByteBuffer data = getData().duplicate();
		data.order(ByteOrder.LITTLE_ENDIAN);
		data.position(data.position() + 24);
		//		data.limit(data.position() + getCaptionLength());
		byte[] chars = new byte[getCaptionLength()];
		data.get(chars);
		return new String(chars, Charset.forName("US-ASCII"));
		//		return data.asCharBuffer().toString();
		//		return ODSUtils.fromLMBCS(data);
	}

	@Override
	public String toString() {
		return "[" + getClass().getSimpleName() + ", FontColor: " + getFontColor() + ", Length: " + getCaptionLength() + ", Caption: "
				+ getCaption() + ", FontID=" + getFontId() + "]";
	}
}
