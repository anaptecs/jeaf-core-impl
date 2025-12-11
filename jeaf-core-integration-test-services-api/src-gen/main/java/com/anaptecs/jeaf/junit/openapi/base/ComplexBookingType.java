/*
 * anaptecs GmbH, Ricarda-Huch-Str. 71, 72760 Reutlingen, Germany
 *
 * Copyright 2004 - 2019. All rights reserved.
 */
package com.anaptecs.jeaf.junit.openapi.base;

/**
 * <p/>
 * <b>Breaking Change with PI 17:</b> Class was changed to an extensible enum. New literals will not be introduced
 * before PI 17.
 *
 * @author JEAF Generator
 * @version JEAF Release 1.4.x
 */
public enum ComplexBookingType {
  COMPLEX, VERY_COMPLEX,
  /**
   * Literal UNKNOWN is used in case that an unknown literal of this enumeration is received e.g. via an external
   * interface.
   */
  UNKNOWN;
}