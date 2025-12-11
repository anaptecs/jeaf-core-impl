/*
 * anaptecs GmbH, Ricarda-Huch-Str. 71, 72760 Reutlingen, Germany
 *
 * Copyright 2004 - 2019. All rights reserved.
 */
package com.anaptecs.jeaf.junit.openapi.base;

public enum Entity {
  DISCOUNT_CAMPAIGN("CAMPGN", DataUnit.COUPON), DISCOUNT_OFFER("DISOFF", DataUnit.COUPON), UNKNOWN("N/A",
      DataUnit.UNKNOWN);

  /**
   * Initialize object.
   */
  private Entity( String pEntityID, DataUnit pDataUnit ) {
    entityID = pEntityID;
    dataUnit = pDataUnit;
  }

  private final String entityID;

  private DataUnit dataUnit;

  /**
   * Method returns attribute {@link #entityID}.<br/>
   *
   * @return {@link String} Value to which {@link #entityID} is set.
   */
  public String getEntityID( ) {
    return entityID;
  }

  /**
   * Method returns association {@link #dataUnit}.<br/>
   *
   * @return {@link DataUnit} Value to which {@link #dataUnit} is set.
   */
  public DataUnit getDataUnit( ) {
    return dataUnit;
  }
}