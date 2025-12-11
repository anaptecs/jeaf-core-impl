
/*
 * anaptecs GmbH, Ricarda-Huch-Str. 71, 72760 Reutlingen, Germany
 *
 * Copyright 2004 - 2019. All rights reserved.
 */
package com.anaptecs.jeaf.junit.impl.core;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.Min;

import com.anaptecs.jeaf.core.api.Component;
import com.anaptecs.jeaf.junit.openapi.base.BeanParameter;
import com.anaptecs.jeaf.junit.openapi.base.BigDecimalCode;
import com.anaptecs.jeaf.junit.openapi.base.BooleanLiteralsEnum;
import com.anaptecs.jeaf.junit.openapi.base.Channel;
import com.anaptecs.jeaf.junit.openapi.base.ChannelCode;
import com.anaptecs.jeaf.junit.openapi.base.ChannelType;
import com.anaptecs.jeaf.junit.openapi.base.Context;
import com.anaptecs.jeaf.junit.openapi.base.CurrencyCode;
import com.anaptecs.jeaf.junit.openapi.base.DeprecatedContext;
import com.anaptecs.jeaf.junit.openapi.base.IntegerCodeType;
import com.anaptecs.jeaf.junit.openapi.base.MultiValuedDataType;
import com.anaptecs.jeaf.junit.openapi.base.NotInlinedBeanParam;
import com.anaptecs.jeaf.junit.openapi.base.ParentBeanParamType;
import com.anaptecs.jeaf.junit.openapi.base.Product;
import com.anaptecs.jeaf.junit.openapi.base.Product.Builder;
import com.anaptecs.jeaf.junit.openapi.base.ShortCode;
import com.anaptecs.jeaf.junit.openapi.base.SpecialContext;
import com.anaptecs.jeaf.junit.openapi.base.StringCode;
import com.anaptecs.jeaf.junit.openapi.base.StringCodeType;
import com.anaptecs.jeaf.junit.openapi.service1.ChildBeanParameterType;
import com.anaptecs.jeaf.junit.openapi.service1.DateQueryParamsBean;
import com.anaptecs.jeaf.junit.openapi.service1.LocalBeanParamType;
import com.anaptecs.jeaf.junit.openapi.service1.TechnicalHeaderContext;
import com.anaptecs.jeaf.junit.rest.generics.BusinessServiceObject;
import com.anaptecs.jeaf.junit.rest.generics.GenericPageableResponse;
import com.anaptecs.jeaf.junit.rest.generics.GenericSingleValuedReponse;
import com.anaptecs.jeaf.junit.rest.generics.Offer;
import com.anaptecs.jeaf.junit.rest.generics.Pageable;
import com.anaptecs.jeaf.junit.rest.generics.Response;
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
  @Override
  public HealthCheckResult check(CheckLevel pLevel) {
    return null;
  }

  @Override
  public List<Product> getProducts( ) {
    List<Product> lProducts = new ArrayList<>();
    Builder lBuilder = Product.builder();
    lBuilder.setProductID(UUID.fromString("c513b71f-433d-4118-be8b-7190226eb155"));
    lProducts.add(lBuilder.build());
    return lProducts;
  }

  @Override
  public Product getProduct(String pProductID) {
    return null;
  }

  @Override
  public boolean createProduct(Product pProduct) {
    return false;
  }

  @Override
  public com.anaptecs.jeaf.junit.openapi.base.Sortiment getSortiment(Context pContext) {
    return null;
  }

  @Override
  public ChannelCode createChannelCode(String pChannelCode) {
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
  public String deprecatedContext(DeprecatedContext pContext) {
    return null;
  }

  @Override
  public void deprecatedBeanParam(BeanParameter pBeanParam) {
  }

  @Deprecated
  @Override
  public String deprecatedParams(@Deprecated
  int pParam1) {
    return null;
  }

  @Override
  public String deprecatedBody(@Deprecated
  String pBody) {
    return null;
  }

  @Override
  public void deprectedComplexRequestBody(@Deprecated
  Product pProduct) {
  }

  @Deprecated
  @Override
  public com.anaptecs.jeaf.junit.openapi.base.Product deprecatedComplexReturn( ) {
    return null;
  }

  @Override
  public void loadSpecificThings(SpecialContext pContext) {
  }

  @Override
  public ChannelCode createChannelCodeFromObject(ChannelCode pChannelCode) {
    return null;
  }

  @Override
  public List<CurrencyCode> addCurrencies(List<CurrencyCode> pCurrencies) {
    return null;
  }

  @Override
  public CurrencyCode isCurrencySupported(CurrencyCode pCurrency) {
    return null;
  }

  @Override
  public IntegerCodeType testCodeTypeUsage(StringCodeType pStringCode) {
    return null;
  }

  @Override
  public String testLocalBeanParamType(LocalBeanParamType pBeanParam) {
    return null;
  }

  @Override
  public String testExternalBeanParameterType(ParentBeanParamType pParent) {
    return null;
  }

  @Override
  public String testChildBeanParameter(ChildBeanParameterType pChild) {
    return null;
  }

  @Override
  public boolean checkIBAN(String pIBAN) {
    return false;
  }

  @Override
  public List<Channel> getChannels(List<ChannelType> pChannelTypes) {
    return null;
  }

  @Override
  public Channel getDefaultChannel(ChannelType pChannelType) {
    return null;
  }

  @Override
  public List<CurrencyCode> getSupportedCurrencies(ChannelCode pChannelCode) {
    return null;
  }

  @Override
  public List<CurrencyCode> getSupportedCurrenciesAsync(ChannelCode pChannelCode) {
    return null;
  }

  @Override
  public void testDateQueryParams(String pPath, OffsetDateTime pStartTimestamp, OffsetTime pStartTime,
      LocalDateTime pLocalStartTimestamp, LocalTime pLocalStartTime, LocalDate pLocalStartDate, Calendar pCalendar,
      Date pUtilDate, Timestamp pSQLTimestamp, Time pSQLTime, java.sql.Date pSQLDate) {
  }

  @Override
  public void testDateQueryParamsBean(String pPath, DateQueryParamsBean pQueryParams) {
  }

  @Override
  public String testOptionalQueryParams(String pQuery1, int pQuery2) {
    return null;
  }

  @Override
  public void testSpecialHeaderParams(String pAuthorization, String pContentType, String pAccept) {
  }

  @Override
  public String testTechnicalHeaderBean(TechnicalHeaderContext pContext) {
    return null;
  }

  @Override
  public String testTechnicalHeaderParam(String pReseller, String pAuthenticationToken) {
    return null;
  }

  @Override
  public void testNotInlinedBeanParam(NotInlinedBeanParam pInlinedBeanParam) {
  }

  @Override
  public void testPrimitiveArray(int[] pIntegerArray) {
  }

  @Override
  public String testPrimitiveArrayAsQueryParam(int[] pIntValues) {
    return null;
  }

  @Override
  public String testMultivaluedHeader(List<BigDecimalCode> pCodes) {
    return null;
  }

  @Override
  public String testMultivaluedQueryParams(List<BigDecimalCode> pCodes, List<BooleanLiteralsEnum> pEnums) {
    return null;
  }

  @Override
  public String testMulitValuedBeanParams(MultiValuedDataType pBeanParam, BooleanLiteralsEnum pTheEnum) {
    return null;
  }

  @Override
  public void noReturnType(String pHeader, MultiValuedDataType pContext) {
  }

  @Override
  public void deleteSomething(String pID) {
  }

  @Override
  public GenericSingleValuedReponse<BusinessServiceObject> genericSingleValueResponse( ) {
    return null;
  }

  @Override
  public GenericPageableResponse<BusinessServiceObject> genericMultiValueResponse( ) {
    return null;
  }

  @Override
  public void testDataTypeWithRestrition(StringCode pStringCode, Set<ShortCode> pShortCodes, @Min(32)
  Byte pJustAByte) {
  }

  @Override
  public void testContext(Context pContext) {
  }

  @Override
  public Response<Pageable<BusinessServiceObject>> testNestedGenericsResponse( ) {
    return null;
  }

  @Override
  public Response<List<Offer>> testNestedMultivaluedResponse( ) {
    return null;
  }

  @Override
  public Response<Offer> testDuplicateGenerics1( ) {
    return null;
  }

  @Override
  public Response<Offer> testDuplicateGenerics2( ) {
    return null;
  }
}
