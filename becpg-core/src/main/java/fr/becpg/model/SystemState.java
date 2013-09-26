/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.model;

/**
 * System state of a node
 * @author querephi
 *
 */
public enum SystemState {
	
		ToValidate,
		Valid,
		Refused,
		Archived;
		
		
		public static SystemState getSystemState(String systemState) {
			
			return (systemState != null && systemState.length()>0) ? SystemState.valueOf(systemState) : SystemState.ToValidate;		
		}
}
