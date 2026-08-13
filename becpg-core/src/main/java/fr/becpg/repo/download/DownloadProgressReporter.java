/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.download;

import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;

/**
 * Reports the progress of a download archive creation.
 *
 * Implemented by the exporters so that the actions driving them can publish a status without knowing
 * how each exporter counts its work.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface DownloadProgressReporter {

	/**
	 * Build the download status to publish for the given state, using the progress counters of the
	 * exporter.
	 *
	 * @param status a {@link org.alfresco.service.cmr.download.DownloadStatus.Status} object
	 * @return a {@link org.alfresco.service.cmr.download.DownloadStatus} object
	 */
	DownloadStatus buildStatus(Status status);

	/**
	 * <p>getNextSequenceNumber.</p>
	 *
	 * @return the sequence number of the next status update
	 */
	int getNextSequenceNumber();

	/**
	 * File extension of the produced archive, used to set the mimetype of the download node.
	 *
	 * @return the extension, or null when the default mimetype has to be kept
	 */
	default String getExtension() {
		return null;
	}

	/**
	 * Release whatever the exporter still holds once the export is over, successful or not.
	 *
	 * An exporter spilling to disk leaves its temporary files behind when the export fails halfway,
	 * so the action calls this on every outcome. Implementations must tolerate being called twice.
	 */
	default void releaseResources() {
		// Nothing to release by default
	}

}
