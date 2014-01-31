/*
Copyright (C) 2010-2014 beCPG. 
 
This file is part of beCPG 
 
beCPG is free software: you can redistribute it and/or modify 
it under the terms of the GNU Lesser General Public License as published by 
the Free Software Foundation, either version 3 of the License, or 
(at your option) any later version. 
 
beCPG is distributed in the hope that it will be useful, 
but WITHOUT ANY WARRANTY; without even the implied warranty of 

MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
GNU Lesser General Public License for more details. 
 
You should have received a copy of the GNU Lesser General Public License 
along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.becpg.repo.action;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;

/*
 * Les règles sur un dossier peuvent être multiple elles sont ordonnées et
 * peuvent être hérités. L'action de gestion des versions contiendrait les
 * paramétrages suivant (cf fichier join):
 * 
 * Version: (Mineur,Majeur,Les deux) Nombre de versions: (Toutes si vide)
 * Nombre de jour à conserver: (Tous si vide) Nombre de versions par jour:
 * (Toutes si vide) Dans le cas de plusieurs règles (héritées ou non) les
 * champs sont surchargés. Exemple :
 * 
 * Règle 1 sur la GED (Les deux, 5 versions Max) Règle 2 sur le Site A
 * (Mineur, vide) Règle 3 sur le Site A (Majeur, 5 versions Max, 1 Version
 * par jour) Règle 4 sur le Site B (Majeur, vide) Donnerait pour un document
 * dans le Site A: Toutes les versions mineures 5 versions Max, 1 Version
 * par jour pour les versions majeurs Pour un document dans le Site B:
 * Toutes les versions majeures 5 versions mineures Max L'application des
 * règles se fait lors de la mise à jour d'un document ou manuellement.
 * Ainsi, même si le nombre de jour maximum est 2, si le document n'est pas
 * modifié les versions restent. Si la règle est modifiée lors de la mise à
 * jour d'un document celui-ci se voit appliquer la nouvelle règle sur
 * toutes les versions de l'historique.
 */

public class VersionCleanerActionConfig {

	private enum VersionCleanerType {
		major, minor, all
	}

	Integer numberOfVersionMinor;
	Integer numberOfDayMinor;
	Integer numberByDayMinor;
	Integer numberOfVersionMajor;
	Integer numberOfDayMajor;
	Integer numberByDayMajor;

	public void setConfig(String versionTypeString, Integer numberOfVersion, Integer numberOfDay, Integer numberByDay) {
		VersionCleanerType versionType = VersionCleanerType.valueOf(versionTypeString);

		if (VersionCleanerType.major.equals(versionType) || VersionCleanerType.all.equals(versionType)) {
			numberOfDayMajor = numberOfDay;
			numberByDayMajor = numberByDay;
			numberOfVersionMajor = numberOfVersion;
		} 
		
		if (VersionCleanerType.minor.equals(versionType) || VersionCleanerType.all.equals(versionType)) {
			numberOfDayMinor = numberOfDay;
			numberByDayMinor = numberByDay;
			numberOfVersionMinor = numberOfVersion;
		}

	}

	private List<Version> versionToKeep(Collection<Version> versions, VersionType versionType, Integer numberOfDay, Integer numberByDay, Integer numberOfVersion) {
		Map<Long, List<Version>> cacheByDay = new LinkedHashMap<Long, List<Version>>();
		List<Version> majorToKeep = new ArrayList<>();

		for (Version version : versions) {
			if (versionType.equals(version.getVersionType())) {
				Long key = removeTime(version.getFrozenModifiedDate()).getTime();

				List<Version> versionByDay = cacheByDay.get(key);
				if (versionByDay == null) {
					versionByDay = new LinkedList<>();
				}
				versionByDay.add(version);
				cacheByDay.put(key, versionByDay);
			}
		}

		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		if (numberOfDay != null) {
			cal.add(Calendar.DATE, -numberOfDay);
		}

		for (Map.Entry<Long, List<Version>> entry : cacheByDay.entrySet()) {
			if (numberOfDay == null || entry.getKey() > cal.getTimeInMillis()) {
				List<Version> versionByDay = entry.getValue();
				if (numberByDay != null) {
					// Most recent first
					versionByDay = versionByDay.subList(0, Math.min(versionByDay.size(), numberByDay));
				}
				majorToKeep.addAll(versionByDay);
			}
		}

		if (numberOfVersion != null) {
			// Most recent first
			majorToKeep = majorToKeep.subList(0, Math.min(majorToKeep.size(), numberOfVersion));
		}

		return majorToKeep;
	}

	/**
	 * @param allVersions
	 * @return
	 */
	public Collection<Version> versionsToDelete(Collection<Version> versions) {

		List<Version> ret = new ArrayList<>(versions);
		List<Version> majorToKeep = versionToKeep(versions, VersionType.MAJOR, numberOfDayMajor, numberByDayMajor, numberOfVersionMajor);
		List<Version> minorToKeep = new LinkedList<>();

		List<Version> tmp = new LinkedList<>();
		for (Version version : versions) {
			if (VersionType.MINOR.equals(version.getVersionType())) {
				tmp.add(version);
			} else {
				if (majorToKeep.contains(version)) {
					minorToKeep.addAll(versionToKeep(tmp, VersionType.MINOR, numberOfDayMinor, numberByDayMinor, numberOfVersionMinor));
				}
				tmp = new LinkedList<>();
			}
		}
		

		ret.removeAll(majorToKeep);
		ret.removeAll(minorToKeep);

		return ret;
	}

	public Date removeTime(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	@Override
	public String toString() {
		return "VersionConfig [numberOfVersionMinor=" + numberOfVersionMinor + ", numberOfDayMinor=" + numberOfDayMinor + ", numberByDayMinor=" + numberByDayMinor
				+ ", numberOfVersionMajor=" + numberOfVersionMajor + ", numberOfDayMajor=" + numberOfDayMajor + ", numberByDayMajor=" + numberByDayMajor + "]";
	}

}
