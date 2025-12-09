/*
 * anaptecs GmbH, Burgstr. 96, 72764 Reutlingen, Germany
 * 
 * Copyright 2004 - 2013 All rights reserved.
 */
package com.anaptecs.jeaf.application.base;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.StringTokenizer;

import com.anaptecs.jeaf.tools.api.collections.CollectionTools;
import com.anaptecs.jeaf.xfun.api.checks.Assert;
import com.anaptecs.jeaf.xfun.api.checks.Check;

/**
 * Class implements interface FilenameFilter to filter all files by its extension. The class implements two ways to
 * filter files. By default all files that do have the defined extensions are accepted. In addition the class also
 * provides the possibility to specify an exclusion list of explicit file names.
 * 
 * @author JEAF Development Team
 * @version 1.0
 */
public class FileFilter implements FilenameFilter {
  /**
   * List contains the extensions of all file types that are accepted by the filter. The list is never null and contains
   * at least one element.
   */
  private final List<String> acceptedExtensions;

  /**
   * List contains the names of all files that should be excluded by the filter.
   */
  private final List<String> exclusionList;

  /**
   * Constructor initializes the object. Thereby no actions are performed. The object does not use an exclusion list.
   * 
   * @param pAcceptedExtensions List contains the white list for all types that should be accepted by the filter. The
   * parameter must not be null and must contain at least one element.
   */
  public FileFilter( List<String> pAcceptedExtensions ) {
    // Check parameter.
    Check.checkInvalidParameterNull(pAcceptedExtensions, "pAcceptedExtensions");
    Check.checkMinimumCollectionSize(pAcceptedExtensions, 1);

    acceptedExtensions = CollectionTools.getCollectionTools().unmodifiableClone(pAcceptedExtensions);
    exclusionList = Collections.emptyList();
  }

  /**
   * Constructor initializes the filter. Thereby a set of excluded files is passed to the object. The exclusion list is
   * an additional feature of the filter in addition to the extension based filtering.
   * 
   * @param pAcceptedExtensions List contains the white list for all types that should be accepted by the filter. The
   * parameter must not be null and must contain at least one element.
   * @param pExclusionList List contains names of all files that should be excluded by the filter. The file names must
   * not contain any path information. The parameter must not be null.
   */
  public FileFilter( List<String> pAcceptedExtensions, List<String> pExclusionList ) {
    // Check pExclusionList for null.
    Check.checkInvalidParameterNull(pAcceptedExtensions, "pAcceptedExtensions");
    Check.checkMinimumCollectionSize(pAcceptedExtensions, 1);
    Assert.assertNotNull(pExclusionList, "pExclusionList");

    acceptedExtensions = CollectionTools.getCollectionTools().unmodifiableClone(pAcceptedExtensions);

    // Split all strings that are separated by ";"
    exclusionList = new ArrayList<String>();
    for (String lNextParam : pExclusionList) {
      StringTokenizer lTokenizer = new StringTokenizer(lNextParam, ";");
      while (lTokenizer.hasMoreTokens()) {
        exclusionList.add(lTokenizer.nextToken().trim());
      }
    }
  }

  /**
   * Method checks whether the file with the passed name is a accepted by the filter or not.
   * 
   * @param pDirectory File object representing the directory in which the file was found. The parameter is never used.
   * @param pFilename Name of the file that should be checked for compliance with this filter. The parameter must not be
   * null.
   * @return boolean Method returns true if pFile is accepted by the filter and false in all other cases.
   * 
   * @see java.io.FileFilter#accept(java.io.File)
   */
  public boolean accept( File pDirectory, String pFilename ) {
    // Check pDirectory and pFileName for null.
    Assert.assertNotNull(pFilename, "pFileName");

    // Filter accepts only files that have one of the accepted extensions.
    boolean lAcceptedExtension = false;
    for (String lExtension : acceptedExtensions) {
      lAcceptedExtension = pFilename.toUpperCase().endsWith(lExtension.toUpperCase());
      if (lAcceptedExtension == true) {
        break;
      }
    }

    // Check if the file was not explicitly excluded.
    boolean lAcceptedFile;
    if (lAcceptedExtension == true) {
      // The passed file is accepted if it is not specified in the exclusion list.
      lAcceptedFile = !exclusionList.contains(pFilename);
    }
    else {
      lAcceptedFile = false;
    }
    return lAcceptedFile;
  }
}
