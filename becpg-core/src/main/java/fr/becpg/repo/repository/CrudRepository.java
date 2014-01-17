/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.repo.repository;

/*
 * Copyright 2008-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import java.io.Serializable;


/**
 * Interface for generic CRUD operations on a repository for a specific type.
 *
 * @author Oliver Gierke
 * @author Eberhard Wolff
 */
public interface CrudRepository<T, ID extends Serializable>  {

	/**
	 * Saves a given entity. Use the returned instance for further operations as
	 * the save operation might have changed the entity instance completely.
	 *
	 * @param entity
	 * @return the saved entity
	 */
	T save(T entity);


	/**
	 * Saves all given entities.
	 *
	 * @param entities
	 * @return
	 */
	Iterable<T> save(Iterable<? extends T> entities);


	/**
	 * Retrives an entity by its primary key.
	 *
	 * @param id
	 * @return the entity with the given primary key or {@code null} if none
	 *         found
	 * @throws IllegalArgumentException if primaryKey is {@code null}
	 */
	T findOne(ID id);


	/**
	 * Returns whether an entity with the given id exists.
	 *
	 * @param id
	 * @return true if an entity with the given id exists, alse otherwise
	 * @throws IllegalArgumentException if primaryKey is {@code null}
	 */
	boolean exists(ID id);


	/**
	 * Returns all instances of the type.
	 *
	 * @return all entities
	 */
	Iterable<T> findAll();


	/**
	 * Returns the number of entities available.
	 *
	 * @return the number of entities
	 */
	long count();

	
	/**
	 * Deletes the entity with the given id.
	 * 
	 * @param id
	 */
	void delete(ID id);

	
	/**
	 * Deletes a given entity.
	 *
	 * @param entity
	 */
	void delete(T entity);

	
	/**
	 * Deletes the given entities.
	 *
	 * @param entities
	 */
	void delete(Iterable<? extends T> entities);


	/**
	 * Deletes all entities managed by the repository.
	 */
	void deleteAll();


}
