package org.openntf.domino.graph2.builtin.social;

import java.util.List;

import org.openntf.domino.graph2.annotations.AdjacencyUnique;
import org.openntf.domino.graph2.annotations.IncidenceUnique;
import org.openntf.domino.graph2.annotations.TypedProperty;
import org.openntf.domino.graph2.builtin.DVertexFrame;

import com.tinkerpop.blueprints.Direction;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.frames.modules.javahandler.JavaHandler;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerClass;
import com.tinkerpop.frames.modules.javahandler.JavaHandlerContext;
import com.tinkerpop.frames.modules.typedgraph.TypeValue;

@TypeValue("Rateable")
@JavaHandlerClass(Rateable.RateableImpl.class)
public interface Rateable extends DVertexFrame {
	@TypedProperty("Rating")
	@JavaHandler
	public double getRaterRating(Rater rater);

	@TypedProperty("AverageRating")
	@JavaHandler
	public double getAverageRating();

	@AdjacencyUnique(label = Rates.LABEL, direction = Direction.IN)
	public List<Rater> getRaters();

	@AdjacencyUnique(label = Rates.LABEL, direction = Direction.IN)
	public Rates addRater(Rater rater);

	@AdjacencyUnique(label = Rates.LABEL, direction = Direction.IN)
	public void removeRater(Rater rater);

	@AdjacencyUnique(label = Rates.LABEL, direction = Direction.IN)
	public Rates findRates(Rater rater);

	@IncidenceUnique(label = Rates.LABEL, direction = Direction.IN)
	public List<Rates> getRates();

	@IncidenceUnique(label = Rates.LABEL, direction = Direction.IN)
	public int countRates();

	@IncidenceUnique(label = Rates.LABEL, direction = Direction.IN)
	public void removeRates(Rates rates);

	public abstract static class RateableImpl implements Rateable, JavaHandlerContext<Vertex> {
		private transient double avgRating_;

		@Override
		public double getAverageRating() {
			List<Rates> rates = getRates();
			if (rates != null) {
				long count = 0l;
				long total = 0l;
				for (Rates rate : rates) {
					count++;
					total += rate.getRating();
				}
				if (count > 0) {
					avgRating_ = total / count;
				}
			}
			return avgRating_;
		}

		@Override
		public double getRaterRating(final Rater rater) {
			Rates raterRates = findRates(rater);
			if (raterRates != null) {
				return raterRates.getRating();
			}
			return getAverageRating();
		}
	}
}
