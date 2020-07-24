/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.model;

/**
 * System state of a node
 *
 * @author querephi
 * @version $Id: $Id
 */
public enum SystemState {
	
		Simulation,
		ToValidate,
		Valid,
		Refused,
		Stopped,
		Archived;
		
		
		/**
		 * <p>getSystemState.</p>
		 *
		 * @param systemState a {@link java.lang.String} object.
		 * @return a {@link fr.becpg.model.SystemState} object.
		 */
		public static SystemState getSystemState(String systemState) {	
		   return (systemState != null && systemState.length()>0) ? SystemState.valueOf(systemState) : SystemState.Simulation;		
		}
}
