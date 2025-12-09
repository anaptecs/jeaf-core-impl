
/*
 * anaptecs GmbH, Ricarda-Huch-Str. 71, 72760 Reutlingen, Germany
 * 
 * Copyright 2004 - 2019. All rights reserved.
 */
package com.anaptecs.jeaf.junit.impl.core;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.junit.openapi.base.BeanParameter;
import com.anaptecs.jeaf.junit.openapi.base.Channel;
import com.anaptecs.jeaf.junit.openapi.base.ChannelCode;
import com.anaptecs.jeaf.junit.openapi.base.ChannelType;
import com.anaptecs.jeaf.junit.openapi.base.Context;
import com.anaptecs.jeaf.junit.openapi.base.CurrencyCode;
import com.anaptecs.jeaf.junit.openapi.base.DeprecatedContext;
import com.anaptecs.jeaf.junit.openapi.base.IntegerCodeType;
import com.anaptecs.jeaf.junit.openapi.base.ParentBeanParamType;
import com.anaptecs.jeaf.junit.openapi.base.Product;
import com.anaptecs.jeaf.junit.openapi.base.Product.Builder;
import com.anaptecs.jeaf.junit.openapi.base.SpecialContext;
import com.anaptecs.jeaf.junit.openapi.base.StringCodeType;
import com.anaptecs.jeaf.junit.openapi.service1.ChildBeanParameterType;
import com.anaptecs.jeaf.junit.openapi.service1.LocalBeanParamType;
import com.anaptecs.jeaf.xfun.api.checks.Check;
import com.anaptecs.jeaf.xfun.api.health.CheckLevel;
import com.anaptecs.jeaf.xfun.api.health.HealthCheckResult;

/**
 * Implementation of ProductService.
 */
final class ProductServiceImpl extends ProductServiceImplBase {
  /**
   * Initialize object.
   */
  ProductServiceImpl( Component pComponent ) {
    super(pComponent);
  }

  /**
   * Method checks the current state of the service. Therefore JEAF defines three different check levels: internal
   * Checks, infrastructure checks and external checks. For further details about the check levels {@see CheckLevel}.
   * 
   * @param pLevel Check level on which the check should be performed. The parameter is never null.
   * @return {@link HealthCheckResult} Object describing the result of the check. The method may return null. This means
   * that the service does not implement any checks. In order to use as less memory as possible the method should use
   * the constant {@link HealthCheckResult#CHECK_OK} if no errors or warnings occurred during the check.
   */
  public HealthCheckResult check( CheckLevel pLevel ) {
    return null;
  }

  @Override
  public List<Product> getProducts( ) {
    List<Product> lProducts = new ArrayList<>();
    Builder lBuilder = Product.Builder.newBuilder();
    lBuilder.setProductID(UUID.fromString("c513b71f-433d-4118-be8b-7190226eb155"));
    lProducts.add(lBuilder.build());
    return lProducts;
  }

  @Override
  public Product getProduct( String pProductID ) {
    return null;
  }

  @Override
  public boolean createProduct( Product pProduct ) {
    return false;
  }

  @Override
  public com.anaptecs.jeaf.junit.openapi.base.Sortiment getSortiment( Context pContext ) {
    return null;
  }

  @Override
  public ChannelCode createChannelCode( String pChannelCode ) {
    Check.checkIsRealString(pChannelCode, "pChannelCode");
    return null;
  }

  @Override
  public void ping( ) {
  }

  @Deprecated
  @Override
  public String deprecatedOperation( ) {
    return null;
  }

  @Override
  public String deprecatedContext( DeprecatedContext pContext ) {
    return null;
  }

  @Override
  public void deprecatedBeanParam( BeanParameter pBeanParam ) {
  }

  @Deprecated
  @Override
  public String deprecatedParams( @Deprecated int pParam1 ) {
    return null;
  }

  @Override
  public String deprecatedBody( @Deprecated String pBody ) {
    return null;
  }

  @Override
  public void deprectedComplexRequestBody( @Deprecated Product pProduct ) {
  }

  @Deprecated
  @Override
  public com.anaptecs.jeaf.junit.openapi.base.Product deprecatedComplexReturn( ) {
    return null;
  }

  @Override
  public void loadSpecificThings( SpecialContext pContext ) {
  }

  @Override
  public ChannelCode createChannelCodeFromObject( ChannelCode pChannelCode ) {
    return null;
  }

  @Override
  public List<CurrencyCode> addCurrencies( List<CurrencyCode> pCurrencies ) {
    return null;
  }

  @Override
  public CurrencyCode isCurrencySupported( CurrencyCode pCurrency ) {
    return null;
  }

  @Override
  public IntegerCodeType testCodeTypeUsage( StringCodeType pStringCode ) {
    return null;
  }

  @Override
  public String testLocalBeanParamType( LocalBeanParamType pBeanParam ) {
    return null;
  }

  @Override
  public String testExternalBeanParameterType( ParentBeanParamType pParent ) {
    return null;
  }

  @Override
  public String testChildBeanParameter( ChildBeanParameterType pChild ) {
    return null;
  }

  @Override
  public boolean checkIBAN( String pIBAN ) {
    return false;
  }

  @Override
  public List<Channel> getChannels( List<ChannelType> pChannelTypes ) {
    return null;
  }

  @Override
  public Channel getDefaultChannel( ChannelType pChannelType ) {
    return null;
  }

  @Override
  public List<CurrencyCode> getSupportedCurrencies( ChannelCode pChannelCode ) {
    return null;
  }

  @Override
  public List<CurrencyCode> getSupportedCurrenciesAsync( ChannelCode pChannelCode ) {
    return null;
  }
}
