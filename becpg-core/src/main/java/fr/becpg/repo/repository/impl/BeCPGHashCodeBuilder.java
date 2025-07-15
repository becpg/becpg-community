/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fr.becpg.repo.repository.impl;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.model.AspectAwareDataItem;

/**
 * <p>
 * Assists in implementing {@link java.lang.Object#hashCode()} methods.
 * </p>
 *
 * <p>
 * This class enables a good <code>hashCode</code> method to be built for any
 * class. It follows the rules laid out in the book
 * <a href="http://java.sun.com/docs/books/effective/index.html">Effective
 * Java</a> by Joshua Bloch. Writing a good <code>hashCode</code> method is
 * actually quite difficult. This class aims to simplify the process.
 * </p>
 *
 * <p>
 * The following is the approach taken. When appending a data field, the current
 * total is multiplied by the multiplier then a relevant value for that data
 * type is added. For example, if the current hashCode is 17, and the multiplier
 * is 37, then appending the integer 45 will create a hashcode of 674, namely 17
 * * 37 + 45.
 * </p>
 *
 * <p>
 * All relevant fields from the object should be included in the
 * <code>hashCode</code> method. Derived fields may be excluded. In general, any
 * field used in the <code>equals</code> method must be used in the
 * <code>hashCode</code> method.
 * </p>
 *
 * <p>
 * To use this class write code as follows:
 * </p>
 *
 * <pre>
 * public class Person {
 *   String name;
 *   int age;
 *   boolean smoker;
 *   ...
 *
 *   public int hashCode() {
 *     // you pick a hard-coded, randomly chosen, non-zero, odd number
 *     // ideally different for each class
 *     return new BeCPGHashCodeBuilder(17, 37).
 *       append(name).
 *       append(age).
 *       append(smoker).
 *       toHashCode();
 *   }
 * }
 * </pre>
 *
 * <p>
 * If required, the superclass <code>hashCode()</code> can be added using appendSuper.
 * </p>
 *
 * <p>
 * Alternatively, there is a method that uses reflection to determine the fields
 * to test. Because these fields are usually private, the method,
 * <code>reflectionHashCode</code>, uses
 * <code>AccessibleObject.setAccessible</code> to change the visibility of the
 * fields. This will fail under a security manager, unless the appropriate
 * permissions are set up correctly. It is also slower than testing explicitly.
 * </p>
 *
 * <p>
 * A typical invocation for this method would look like:
 * </p>
 *
 * <pre>
 * public int hashCode() {
 * 	return BeCPGHashCodeBuilder.reflectionHashCode(this);
 * }
 * </pre>
 *
 * @author Apache Software Foundation
 * @author Gary Gregory
 * @author Pete Gieser
 * @author Matthieu L
 * @since 1.0
 * @version $Id: BeCPGHashCodeBuilder.java 1057009 2011-01-09 19:48:06Z niallp $
 *          beCPG FIX when Integer, Float or Double is null give same hashCode
 *          as when egal 0
 */
public class BeCPGHashCodeBuilder {

	/**
	 * Constant to use in building the hashCode.
	 */
	private static final int iConstant = 37;
	private static final int NULL_MULTIPLIER = iConstant + 12;
	private static final Map<Class<?>, List<Method>> annotatedMethodsCache = new ConcurrentHashMap<>();

	private long reflectionAppend(RepositoryEntity object, Set<RepositoryEntity> visited) {
		long total = 17;

		if (visited.contains(object)) {
			return total;
		}

		visited.add(object);

		Class<?> clazz = object.getClass();
		List<Method> methods = annotatedMethodsCache.computeIfAbsent(clazz, this::getCachedMethods);

		for (Method method : methods) {
			try {
				Object fieldValue = method.invoke(object);
				total = append(total, fieldValue, visited);
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				// Log error but continue processing
				e.printStackTrace();
			}
		}

		total = append(total, object.getNodeRef(), visited);

		if (object instanceof AspectAwareDataItem aspectAwareDataItem) {
			total = appendAspects(total, aspectAwareDataItem, visited);
		}

		return total;
	}

	/**
	 * Extract method caching logic for better readability
	 */
	private List<Method> getCachedMethods(Class<?> clazz) {
		List<Method> methodList = new ArrayList<>();
		
		// Use a dummy object to get property descriptors - this is safe since we only need metadata
		Object dummyObject;
		try {
			dummyObject = clazz.getDeclaredConstructor().newInstance();
		} catch (Exception e) {
			// Fallback: return empty list if we can't create a dummy object
			return methodList;
		}
		
		BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(dummyObject);
		for (PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
			Method readMethod = pd.getReadMethod();
			if (readMethod != null && isAnnotatedMethod(readMethod)) {
				methodList.add(readMethod);
			}
		}
		return methodList;
	}

	/**
	 * Check if method has relevant annotations
	 */
	private boolean isAnnotatedMethod(Method method) {
		return method.isAnnotationPresent(AlfProp.class) || 
		       method.isAnnotationPresent(AlfSingleAssoc.class) || 
		       method.isAnnotationPresent(AlfMultiAssoc.class);
	}

	/**
	 * Optimized aspect handling
	 */
	private long appendAspects(long total, AspectAwareDataItem aspectAwareDataItem, Set<RepositoryEntity> visited) {
		Set<QName> aspects = aspectAwareDataItem.getAspects();
		if (aspects == null || aspects.isEmpty()) {
			return total;
		}

		Set<QName> aspectsToRemove = aspectAwareDataItem.getAspectsToRemove();
		int aspectsHash = 0;
		
		for (QName aspect : aspects) {
			if (aspect != null && !aspectsToRemove.contains(aspect)) {
				aspectsHash += aspect.hashCode();
			}
		}
		
		return append(total, aspectsHash, visited);
	}

	/**
	 * <p>printDiff.</p>
	 *
	 * @param obj1 a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @param obj2 a {@link fr.becpg.repo.repository.RepositoryEntity} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String printDiff(RepositoryEntity obj1, RepositoryEntity obj2) {
		StringBuilder ret = new StringBuilder();

		BeanWrapper beanWrapper1 = PropertyAccessorFactory.forBeanPropertyAccess(obj1);
		BeanWrapper beanWrapper2 = PropertyAccessorFactory.forBeanPropertyAccess(obj2);

		BeCPGHashCodeBuilder builder1 = new BeCPGHashCodeBuilder();
		BeCPGHashCodeBuilder builder2 = new BeCPGHashCodeBuilder();

		Set<RepositoryEntity> visited1 = new HashSet<>();
		Set<RepositoryEntity> visited2 = new HashSet<>();

		for (PropertyDescriptor pd : beanWrapper1.getPropertyDescriptors()) {
			Method readMethod = pd.getReadMethod();
			if (readMethod != null && builder1.isAnnotatedMethod(readMethod)) {
				Object fieldValue = beanWrapper1.getPropertyValue(pd.getName());
				Object fieldValue2 = beanWrapper2.getPropertyValue(pd.getName());

				long total1 = builder1.append(17, fieldValue, visited1);
				long total2 = builder2.append(17, fieldValue2, visited2);

				if (total1 != total2) {
					ret.append("\n append :").append(pd.getName()).append("  hash ").append(total1).append("/").append(total2).append("\n");
					appendDiffDetails(ret, fieldValue, fieldValue2, obj1, obj2);
				}
			}
		}

		appendNodeRefDiff(ret, obj1, obj2);
		appendAspectsDiff(ret, obj1, obj2);

		return ret.toString();
	}

	/**
	 * Extract diff details logic for better readability
	 */
	private static void appendDiffDetails(StringBuilder ret, Object fieldValue, Object fieldValue2, RepositoryEntity obj1, RepositoryEntity obj2) {
		if (fieldValue instanceof RepositoryEntity repoEntity && fieldValue2 instanceof RepositoryEntity repoEntity2) {
			// skip recursion
			if (!obj1.equals(fieldValue) || !obj2.equals(fieldValue2)) {
				ret.append("\n-- Recur diff ");
				ret.append(printDiff(repoEntity, repoEntity2));
			}
		} else if (fieldValue instanceof List<?> list1 && fieldValue2 instanceof List<?> list2) {
			ret.append("\n-- Recur list:  ").append(list1.size()).append(" ").append(list2.size());
			appendListDiff(ret, list1, list2);
		} else {
			ret.append(" --- To save ").append(fieldValue != null ? fieldValue.toString() : "null")
			   .append("/ Saved ").append(fieldValue2 != null ? fieldValue2.toString() : "null")
			   .append(" ").append(fieldValue != null ? fieldValue.getClass().getName() : "").append("\n");
		}
	}

	/**
	 * Extract list diff logic
	 */
	private static void appendListDiff(StringBuilder ret, List<?> list1, List<?> list2) {
		boolean printList = false;
		for (Object el : list2) {
			if (el instanceof RepositoryEntity repoEntity) {
				for (Object el2 : list1) {
					if (el2 instanceof RepositoryEntity repoEntity2 && 
					    repoEntity.getNodeRef().equals(repoEntity2.getNodeRef())) {
						ret.append("-- Recur diff ");
						ret.append(printDiff(repoEntity, repoEntity2));
					}
				}
			} else {
				printList = true;
			}
		}
		if (printList) {
			ret.append(list1.toString()).append(" - ").append(list2.toString());
		}
	}

	/**
	 * Extract nodeRef diff logic
	 */
	private static void appendNodeRefDiff(StringBuilder ret, RepositoryEntity obj1, RepositoryEntity obj2) {
		if (obj1.getNodeRef() != null && !obj1.getNodeRef().equals(obj2.getNodeRef())) {
			ret.append("nodeRef differs\n");
		}
	}

	/**
	 * Extract aspects diff logic
	 */
	private static void appendAspectsDiff(StringBuilder ret, RepositoryEntity obj1, RepositoryEntity obj2) {
		int tmp1 = calculateAspectsHash(obj1);
		int tmp2 = calculateAspectsHash(obj2);

		if (tmp1 != tmp2) {
			ret.append("aspect differs:\n");
			if (obj1 instanceof AspectAwareDataItem aspectAware1 && aspectAware1.getAspects() != null) {
				ret.append(" ---- To save ").append(aspectAware1.getAspects().toString()).append("\n");
			}
			if (obj2 instanceof AspectAwareDataItem aspectAware2 && aspectAware2.getAspects() != null) {
				ret.append(" ---- Saved ").append(aspectAware2.getAspects().toString()).append("\n");
			}
		}
	}

	/**
	 * Calculate aspects hash for diff comparison
	 */
	private static int calculateAspectsHash(RepositoryEntity obj) {
		int tmp = 0;
		if (obj instanceof AspectAwareDataItem aspectAwareDataItem && aspectAwareDataItem.getAspects() != null) {
			for (QName aspect : aspectAwareDataItem.getAspects()) {
				if (aspect != null && !aspectAwareDataItem.getAspectsToRemove().contains(aspect)) {
					tmp += aspect.hashCode();
				}
			}
		}
		return tmp;
	}

	/**
	 * <p>
	 * This method uses reflection to build a valid hash code.
	 * </p>
	 *
	 * <p>
	 * This constructor uses two hard coded choices for the constants needed to
	 * build a hash code.
	 * </p>
	 *
	 * <p>
	 * It uses <code>AccessibleObject.setAccessible</code> to gain access to
	 * private fields. This means that it will throw a security exception if run
	 * under a security manager, if the permissions are not set up correctly. It
	 * is also not as efficient as testing explicitly.
	 * </p>
	 *
	 * <p>
	 * Transient members will be not be used, as they are likely derived fields,
	 * and not part of the value of the <code>Object</code>.
	 * </p>
	 *
	 * <p>
	 * Static fields will not be tested. Superclass fields will be included.
	 * </p>
	 *
	 * @param object
	 *            the Object to create a <code>hashCode</code> for
	 * @return int hash code
	 * @throws java.lang.IllegalArgumentException
	 *             if the object is <code>null</code>
	 */
	public static long reflectionHashCode(RepositoryEntity object) {
		if (object == null) {
			throw new IllegalArgumentException("The object to build a hash code for must not be null");
		}
		BeCPGHashCodeBuilder builder = new BeCPGHashCodeBuilder();
		return builder.reflectionAppend(object, new HashSet<>());
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>boolean</code>.
	 * </p>
	 * <p>
	 * This adds <code>1</code> when true, and <code>0</code> when false to the
	 * <code>hashCode</code>.
	 * </p>
	 * <p>
	 * This is in contrast to the standard
	 * <code>java.lang.Boolean.hashCode</code> handling, which computes a
	 * <code>hashCode</code> value of <code>1231</code> for
	 * <code>java.lang.Boolean</code> instances that represent <code>true</code>
	 * or <code>1237</code> for <code>java.lang.Boolean</code> instances that
	 * represent <code>false</code>.
	 * </p>
	 * <p>
	 * This is in accordance with the <quote>Effective Java</quote> design.
	 * </p>
	 *
	 * @param value
	 *            the boolean to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, boolean value) {
		return (total * iConstant) + (value ? 0 : 1);
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>boolean</code> array.
	 * </p>
	 *
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, boolean[] array) {
		if (array == null) {
			return total * iConstant;
		}
		for (boolean element : array) {
			total = append(total, element);
		}
		return total;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>byte</code>.
	 * </p>
	 *
	 * @param value
	 *            the byte to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, byte value) {
		return (total * iConstant) + value;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>byte</code> array.
	 * </p>
	 *
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, byte[] array) {
		if (array == null) {
			return total * iConstant;
		}
		for (byte element : array) {
			total = append(total, element);
		}
		return total;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>char</code>.
	 * </p>
	 *
	 * @param value
	 *            the char to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, char value) {
		return (total * iConstant) + value;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>char</code> array.
	 * </p>
	 *
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, char[] array) {
		if (array == null) {
			return total * iConstant;
		}
		for (char element : array) {
			total = append(total, element);
		}
		return total;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>double</code>.
	 * </p>
	 *
	 * @param value
	 *            the double to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, double value) {
		return append(total, Double.doubleToLongBits(value));
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>double</code> array.
	 * </p>
	 *
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, double[] array) {
		if (array == null) {
			return total * iConstant;
		}
		for (double element : array) {
			total = append(total, element);
		}
		return total;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>float</code>.
	 * </p>
	 *
	 * @param value
	 *            the float to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, float value) {
		return (total * iConstant) + Float.floatToIntBits(value);
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>float</code> array.
	 * </p>
	 *
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, float[] array) {
		if (array == null) {
			return total * iConstant;
		}
		for (float element : array) {
			total = append(total, element);
		}
		return total;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for an <code>int</code>.
	 * </p>
	 *
	 * @param value
	 *            the int to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, int value) {
		return (total * iConstant) + value;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for an <code>int</code> array.
	 * </p>
	 *
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, int[] array) {
		if (array == null) {
			return total * iConstant;
		}
		for (int element : array) {
			total = append(total, element);
		}
		return total;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>long</code>.
	 * </p>
	 *
	 * @param value
	 *            the long to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, long value) {
		return (total * iConstant) + ((int) (value ^ (value >> 32)));
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>long</code> array.
	 * </p>
	 *
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, long[] array) {
		if (array == null) {
			return total * iConstant;
		}
		for (long element : array) {
			total = append(total, element);
		}
		return total;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for an <code>Object</code>.
	 * </p>
	 *
	 * @param object
	 *            the Object to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, Object object, Set<RepositoryEntity> visited) {
		if (object == null || (object instanceof MLText mlText && mlText.isEmpty())) {
			// beCPG avoid collision on NULL == 0 #1159
			return total * NULL_MULTIPLIER;
		}
		
		if (object.getClass().isArray()) {
			return appendArray(total, object, visited);
		}
		
		if (object instanceof RepositoryEntity repoEntity) {
			return (total * iConstant) + reflectionAppend(repoEntity, visited);
		}
		
		if (object instanceof Collection<?> collection) {
			for (Object el : collection) {
				total = append(total, el, visited);
			}
			return total;
		}
		
		return (total * iConstant) + object.hashCode();
	}

	/**
	 * Optimized array handling
	 */
	private long appendArray(long total, Object object, Set<RepositoryEntity> visited) {
		// Handle primitive arrays more efficiently
		if (object instanceof int[] array) {
			return append(total, array);
		} else if (object instanceof long[] array) {
			return append(total, array);
		} else if (object instanceof short[] array) {
			return append(total, array);
		} else if (object instanceof char[] array) {
			return append(total, array);
		} else if (object instanceof byte[] array) {
			return append(total, array);
		} else if (object instanceof double[] array) {
			return append(total, array);
		} else if (object instanceof float[] array) {
			return append(total, array);
		} else if (object instanceof boolean[] array) {
			return append(total, array);
		} else {
			// Object array
			return append(total, (Object[]) object, visited);
		}
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for an <code>Object</code> array.
	 * </p>
	 *
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 * @param total a long.
	 * @param visited a {@link java.util.Set} object.
	 */
	public long append(long total, Object[] array, Set<RepositoryEntity> visited) {
		if (array == null) {
			return total * iConstant;
		}
		for (Object element : array) {
			total = append(total, element, visited);
		}
		return total;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>short</code>.
	 * </p>
	 *
	 * @param value
	 *            the short to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, short value) {
		return (total * iConstant) + value;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>short</code> array.
	 * </p>
	 *
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 */
	private long append(long total, short[] array) {
		if (array == null) {
			return total * iConstant;
		}
		for (short element : array) {
			total = append(total, element);
		}
		return total;
	}
}