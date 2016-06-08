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
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
 * Assists in implementing {@link Object#hashCode()} methods.
 * </p>
 * 
 * <p>
 * This class enables a good <code>hashCode</code> method to be built for any
 * class. It follows the rules laid out in the book <a
 * href="http://java.sun.com/docs/books/effective/index.html">Effective Java</a>
 * by Joshua Bloch. Writing a good <code>hashCode</code> method is actually
 * quite difficult. This class aims to simplify the process.
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
 * If required, the superclass <code>hashCode()</code> can be added using
 * {@link #appendSuper}.
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
	 * <p>
	 * A registry of objects used by reflection methods to detect cyclical
	 * object references and avoid infinite loops.
	 * </p>
	 * 
	 * @since 2.3
	 */
	private static final ThreadLocal<Set<Object>> REGISTRY = new ThreadLocal<>();

	/*
	 * N.B. we cannot store the actual objects in a HashSet, as that would use
	 * the very hashCode() we are in the process of calculating.
	 * 
	 * So we generate a one-to-one mapping from the original object to a new
	 * object.
	 * 
	 * Now HashSet uses equals() to determine if two elements with the same
	 * hashcode really are equal, so we also need to ensure that the replacement
	 * objects are only equal if the original objects are identical.
	 * 
	 * The original implementation (2.4 and before) used the
	 * System.indentityHashCode() method - however this is not guaranteed to
	 * generate unique ids (e.g. LANG-459)
	 * 
	 * We now use the IDKey helper class (adapted from
	 * org.apache.axis.utils.IDKey) to disambiguate the duplicate ids.
	 */

	/**
	 * <p>
	 * Returns the registry of objects being traversed by the reflection methods
	 * in the current thread.
	 * </p>
	 * 
	 * @return Set the registry of objects being traversed
	 * @since 2.3
	 */
	private static Set<Object> getRegistry() {
		return REGISTRY.get();
	}

	
	/**
	 * Constant to use in building the hashCode.
	 */
	private final int iConstant;

	/**
	 * Running total of the hashCode.
	 */
	private int iTotal = 0;

	/**
	 * <p>
	 * Uses two hard coded choices for the constants needed to build a
	 * <code>hashCode</code>.
	 * </p>
	 */
	private BeCPGHashCodeBuilder() {
		iConstant = 37;
		iTotal = 17;
	}

	
	
	/**
	 * <p>
	 * Returns <code>true</code> if the registry contains the given object. Used
	 * by the reflection methods to avoid infinite loops.
	 * </p>
	 * 
	 * @param value
	 *            The object to lookup in the registry.
	 * @return boolean <code>true</code> if the registry contains the given
	 *         object.
	 * @since 2.3
	 */
	private static boolean isRegistered(Object value) {
		Set<Object> registry = getRegistry();
		return registry != null && registry.contains(new IDKey(value));
	}

	private static void reflectionAppend(RepositoryEntity object, BeCPGHashCodeBuilder builder) {
		if (isRegistered(object)) {
			return;
		}
		try {
			register(object);

			BeanWrapper beanWrapper = PropertyAccessorFactory.forBeanPropertyAccess(object);

			for (PropertyDescriptor pd : beanWrapper.getPropertyDescriptors()) {
				Method readMethod = pd.getReadMethod();
				if (readMethod != null) {
					if (readMethod.isAnnotationPresent(AlfProp.class) || readMethod.isAnnotationPresent(AlfSingleAssoc.class)
							|| readMethod.isAnnotationPresent(AlfMultiAssoc.class)) {
						Object fieldValue = beanWrapper.getPropertyValue(pd.getName());

						builder.append(fieldValue);
					}
				}
			}

			builder.append(object.getNodeRef());

			if (object instanceof AspectAwareDataItem 
					&& ((AspectAwareDataItem) object).getAspects() != null) {
					int tmp = 0;
					for (QName aspect : ((AspectAwareDataItem) object).getAspects()) {
						if(aspect!=null){
							tmp+=aspect.hashCode();
						}
					}
					builder.append(tmp);
			}

		} finally {
			unregister(object);
		}
	}

	public static String printDiff(RepositoryEntity obj1, RepositoryEntity obj2) {
		String ret = new String();

		BeanWrapper beanWrapper1 = PropertyAccessorFactory.forBeanPropertyAccess(obj1);
		BeanWrapper beanWrapper2 = PropertyAccessorFactory.forBeanPropertyAccess(obj2);

		BeCPGHashCodeBuilder builder1 = new BeCPGHashCodeBuilder();
		BeCPGHashCodeBuilder builder2 = new BeCPGHashCodeBuilder();

		for (PropertyDescriptor pd : beanWrapper1.getPropertyDescriptors()) {
			Method readMethod = pd.getReadMethod();
			if (readMethod != null) {
				if (readMethod.isAnnotationPresent(AlfProp.class) || readMethod.isAnnotationPresent(AlfSingleAssoc.class)
						|| readMethod.isAnnotationPresent(AlfMultiAssoc.class)) {
					Object fieldValue = beanWrapper1.getPropertyValue(pd.getName());
					Object fieldValue2 = beanWrapper2.getPropertyValue(pd.getName());

					builder1.append(fieldValue);
					builder2.append(fieldValue2);

					if (fieldValue!=null && fieldValue2!=null && fieldValue.hashCode() != fieldValue2.hashCode()) {
						ret += pd.getName() + " " + builder1.hashCode() + " " + builder2.hashCode() + "\n";

						if (fieldValue != null) {
							ret += " ----" + fieldValue.toString() + " " + fieldValue.hashCode() + " " + fieldValue.getClass().getName() + "\n";
						}
						if (fieldValue2 != null) {
							ret += " ----" + fieldValue2.toString() + " " + fieldValue2.hashCode() + " " + fieldValue2.getClass().getName() + "\n";
						}
					}

				}
			}
		}

		if(!obj1.getNodeRef().equals(obj2.getNodeRef())){
			ret += "nodeRef differs\n";
		}
		
		int tmp1 = 0;
		int tmp2 = 0;
		if (obj1 instanceof AspectAwareDataItem 
				&& ((AspectAwareDataItem) obj1).getAspects() != null) {
			for (QName aspect : ((AspectAwareDataItem) obj1).getAspects()) {
				if(aspect!=null){
					tmp1+=aspect.hashCode();
				}
			}
		}
		
		if (obj2 instanceof AspectAwareDataItem 
				&& ((AspectAwareDataItem) obj2).getAspects() != null) {
			for (QName aspect : ((AspectAwareDataItem) obj2).getAspects()) {
				if(aspect!=null){
					tmp2+=aspect.hashCode();
				}
			}
		}
		
		if(tmp1!=tmp2){
			ret += "Aspect differs:\n";
			if (((AspectAwareDataItem) obj1).getAspects() != null) {
				ret += " ----" + ((AspectAwareDataItem) obj1).getAspects().toString() + "\n";
			}
			if (((AspectAwareDataItem) obj2).getAspects() != null) {
				ret += " ----" + ((AspectAwareDataItem) obj2).getAspects().toString()  + "\n";
			}
		}
		
		
		return ret;
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
	 * @throws IllegalArgumentException
	 *             if the object is <code>null</code>
	 */
	public static int reflectionHashCode(RepositoryEntity object) {
		if (object == null) {
			throw new IllegalArgumentException("The object to build a hash code for must not be null");
		}
		BeCPGHashCodeBuilder builder = new BeCPGHashCodeBuilder();
		reflectionAppend(object, builder);

		return builder.toHashCode();
	}

	/**
	 * <p>
	 * Registers the given object. Used by the reflection methods to avoid
	 * infinite loops.
	 * </p>
	 * 
	 * @param value
	 *            The object to register.
	 */
	private static void register(Object value) {
		synchronized (BeCPGHashCodeBuilder.class) {
			if (getRegistry() == null) {
				REGISTRY.set(new HashSet<>());
			}
		}
		getRegistry().add(new IDKey(value));
	}

	/**
	 * <p>
	 * Unregisters the given object.
	 * </p>
	 * 
	 * <p>
	 * Used by the reflection methods to avoid infinite loops.
	 * 
	 * @param value
	 *            The object to unregister.
	 * @since 2.3
	 */
	private static void unregister(Object value) {
		Set<Object> registry = getRegistry();
		if (registry != null) {
			registry.remove(new IDKey(value));
			synchronized (BeCPGHashCodeBuilder.class) {
				// read again
				registry = getRegistry();
				if (registry != null && registry.isEmpty()) {
					REGISTRY.set(null);
				}
			}
		}
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
	private BeCPGHashCodeBuilder append(boolean value) {
		iTotal = iTotal * iConstant + (value ? 0 : 1);
		return this;
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
	private BeCPGHashCodeBuilder append(boolean[] array) {
		if (array == null) {
			iTotal = iTotal * iConstant;
		} else {
			for (boolean anArray : array) {
				append(anArray);
			}
		}
		return this;
	}

	// -------------------------------------------------------------------------

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>byte</code>.
	 * </p>
	 * 
	 * @param value
	 *            the byte to add to the <code>hashCode</code>
	 * @return this
	 */
	private BeCPGHashCodeBuilder append(byte value) {
		iTotal = iTotal * iConstant + value;
		return this;
	}

	// -------------------------------------------------------------------------

	/**
	 * <p>
	 * Append a <code>hashCode</code> for a <code>byte</code> array.
	 * </p>
	 * 
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 */
	private BeCPGHashCodeBuilder append(byte[] array) {
		if (array == null) {
			iTotal = iTotal * iConstant;
		} else {
			for (byte anArray : array) {
				append(anArray);
			}
		}
		return this;
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
	private BeCPGHashCodeBuilder append(char value) {
		iTotal = iTotal * iConstant + value;
		return this;
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
	private BeCPGHashCodeBuilder append(char[] array) {
		if (array == null) {
			iTotal = iTotal * iConstant;
		} else {
			for (char anArray : array) {
				append(anArray);
			}
		}
		return this;
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
	private BeCPGHashCodeBuilder append(double value) {
		return append(Double.doubleToLongBits(value));
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
	private BeCPGHashCodeBuilder append(double[] array) {
		if (array == null) {
			iTotal = iTotal * iConstant;
		} else {
			for (double anArray : array) {
				append(anArray);
			}
		}
		return this;
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
	private BeCPGHashCodeBuilder append(float value) {
		iTotal = iTotal * iConstant + Float.floatToIntBits(value);
		return this;
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
	private BeCPGHashCodeBuilder append(float[] array) {
		if (array == null) {
			iTotal = iTotal * iConstant;
		} else {
			for (float anArray : array) {
				append(anArray);
			}
		}
		return this;
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
	private BeCPGHashCodeBuilder append(int value) {
		iTotal = iTotal * iConstant + value;
		return this;
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
	private BeCPGHashCodeBuilder append(int[] array) {
		if (array == null) {
			iTotal = iTotal * iConstant;
		} else {
			for (int anArray : array) {
				append(anArray);
			}
		}
		return this;
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
	// NOTE: This method uses >> and not >>> as Effective Java and
	// Long.hashCode do. Ideally we should switch to >>> at
	// some stage. There are backwards compat issues, so
	// that will have to wait for the time being. cf LANG-342.
	private BeCPGHashCodeBuilder append(long value) {
		iTotal = iTotal * iConstant + ((int) (value ^ (value >> 32)));
		return this;
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
	private BeCPGHashCodeBuilder append(long[] array) {
		if (array == null) {
			iTotal = iTotal * iConstant;
		} else {
			for (long anArray : array) {
				append(anArray);
			}
		}
		return this;
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
	private BeCPGHashCodeBuilder append(Object object) {
		if (object == null) {
			// beCPG avoid collision on NULL == 0 #1159
			iTotal = iTotal * (iConstant + 12);

		} else {
			if (object.getClass().isArray()) {
				// 'Switch' on type of array, to dispatch to the correct handler
				// This handles multi dimensional arrays
				if (object instanceof long[]) {
					append((long[]) object);
				} else if (object instanceof int[]) {
					append((int[]) object);
				} else if (object instanceof short[]) {
					append((short[]) object);
				} else if (object instanceof char[]) {
					append((char[]) object);
				} else if (object instanceof byte[]) {
					append((byte[]) object);
				} else if (object instanceof double[]) {
					append((double[]) object);
				} else if (object instanceof float[]) {
					append((float[]) object);
				} else if (object instanceof boolean[]) {
					append((boolean[]) object);
				} else {
					// Not an array of primitives
					append((Object[]) object);
				}
			} else {
				if (object instanceof RepositoryEntity) {
					iTotal = iTotal * iConstant + BeCPGHashCodeBuilder.reflectionHashCode((RepositoryEntity) object);
				} else if  (object instanceof List) {
					int tmp = 0;
					for(Object el : (List<?>)(object)){
						if(el!=null){
							tmp+=el.hashCode();
						}
					}
					iTotal = iTotal * iConstant + tmp;
				}	else {
					iTotal = iTotal * iConstant + object.hashCode();
				}
			}
		}
		return this;
	}

	/**
	 * <p>
	 * Append a <code>hashCode</code> for an <code>Object</code> array.
	 * </p>
	 * 
	 * @param array
	 *            the array to add to the <code>hashCode</code>
	 * @return this
	 */
	public BeCPGHashCodeBuilder append(Object[] array) {
		if (array == null) {
			iTotal = iTotal * iConstant;
		} else {
			for (Object anArray : array) {
				append(anArray);
			}
		}
		return this;
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
	private BeCPGHashCodeBuilder append(short value) {
		iTotal = iTotal * iConstant + value;
		return this;
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
	private BeCPGHashCodeBuilder append(short[] array) {
		if (array == null) {
			iTotal = iTotal * iConstant;
		} else {
			for (short anArray : array) {
				append(anArray);
			}
		}
		return this;
	}

	
	/**
	 * <p>
	 * Return the computed <code>hashCode</code>.
	 * </p>
	 * 
	 * @return <code>hashCode</code> based on the fields appended
	 */
	private int toHashCode() {
		return iTotal;
	}

	

}
