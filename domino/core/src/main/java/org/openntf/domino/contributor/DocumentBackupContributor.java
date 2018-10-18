package org.openntf.domino.contributor;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.openntf.domino.Database;
import org.openntf.domino.Document;
import org.openntf.domino.utils.Dates;

/**
 * Represents a contributed service that can provide access to time-based backups of documents.
 *
 * @author Jesse Gallagher
 * @since 10.0
 */
public interface DocumentBackupContributor {
	/**
	 * Creates a document in a "sidecar" database representing the state of the original document at the provided point in time, without
	 * modifying the origin DB.
	 *
	 * @param database
	 *            the Database housing the document whose version to restore
	 * @param universalId
	 *            the UNID of the document to restore
	 * @param revisionDate
	 *            the date to find a revision for
	 * @return the sidecar document if the document existed in the proxy DB, or an empty optional otherwise
	 */
	Optional<Document> createSidecarDocument(Database database, String universalId, Date date);

	/**
	 * @return a {@link List} of {@link Dates} for the document's revisions, or an empty optional if the document does not have a proxy
	 */
	Optional<List<Date>> getRevisionDates(Database database, String universalId);
}
