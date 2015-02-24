/*
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * ExcelLoader.java
 * Copyright (C) 2010-2015 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core.converters;

import ucar.nc2.NetcdfFile;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * <!-- globalinfo-start --> Reads a source that is in the Excel spreadsheet
 * format.<br/>
 * For instance, a spreadsheet generated with the Microsoft Office Suite.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -sheet &lt;index&gt;
 *  The index of the sheet to load; 'first' and 'last' are accepted as well.
 * </pre>
 * 
 * <pre>
 * -M &lt;str&gt;
 *  The string representing a missing value.
 *  (default: '')
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * For a tutorial on ExcelDOM, see: <br/>
 * <a href="http://java.dzone.com/news/integrate-openoffice-java"
 * target="_blank">http://java.dzone.com/news/integrate-openoffice-java</a>
 * 
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @author Geertjan Wielenga
 * @version $Revision: 11573 $
 * @see weka.core.converters.Loader
 */
public class NetCDFLoader
  extends AbstractFileLoader
  implements BatchConverter, URLSourcedLoader {

  /** for serialization. */
  private static final long serialVersionUID = 9164120515718983413L;

  /** the .nc file extension. */
  public static String FILE_EXTENSION_NC = ".nc";

  /** the .cdf file extension. */
  public static String FILE_EXTENSION_CDF = ".cdf";

  /** the file description. */
  public static String FILE_DESCRIPTION = "NetCDF files";

  /** the url. */
  protected String m_URL = "http://";

  /**
   * Returns a string describing this Loader.
   *
   * @return a description of the Loader suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String globalInfo() {
    return "Reads data from NetCDF sources.";
  }

  /**
   * Get the file extension used for JSON files.
   *
   * @return the file extension
   */
  @Override
  public String getFileExtension() {
    return FILE_EXTENSION_NC;
  }

  /**
   * Gets all the file extensions used for this type of file.
   *
   * @return the file extensions
   */
  @Override
  public String[] getFileExtensions() {
    return new String[] {FILE_EXTENSION_NC, FILE_EXTENSION_CDF};
  }

  /**
   * Returns a description of the file type.
   *
   * @return a short file description
   */
  @Override
  public String getFileDescription() {
    return FILE_DESCRIPTION;
  }

  /**
   * Resets the Loader ready to read a new data set.
   *
   * @throws java.io.IOException if something goes wrong
   */
  @Override
  public void reset() throws IOException {
    m_structure = null;

    setRetrieval(NONE);

    if (m_File != null) {
      setFile(new File(m_File));
    } else if ((m_URL != null) && !m_URL.equals("http://")) {
      setURL(m_URL);
    }
  }

  /**
   * Resets the Loader object and sets the source of the data set to be the
   * supplied url.
   *
   * @param url the source url.
   * @throws java.io.IOException if an error occurs
   */
  public void setSource(URL url) throws IOException {
    m_structure = null;
    setRetrieval(NONE);
    m_URL = url.toString();
  }

  /**
   * Set the url to load from.
   *
   * @param url the url to load from
   * @throws java.io.IOException if the url can't be set.
   */
  @Override
  public void setURL(String url) throws IOException {
    m_URL = url;
    setSource(new URL(url));
  }

  /**
   * Return the current url.
   *
   * @return the current url
   */
  @Override
  public String retrieveURL() {
    return m_URL;
  }

  /**
   * Determines and returns (if possible) the structure (internally the header)
   * of the data set as an empty set of instances.
   *
   * @return the structure of the data set as an empty set of Instances
   * @throws java.io.IOException if an error occurs
   */
  @Override
  public Instances getStructure() throws IOException {
    if ((m_File == null) && (m_URL == null)) {
      throw new IOException("No source (file/URL) has been specified");
    }

    if (m_structure == null) {
      try {
	NetcdfFile file;
	if (!m_File.isEmpty()) {
	  if (!new File(m_File).exists())
	    throw new IOException("File '" + m_File + "' does not exist!");
	  file = NetcdfFile.open(m_File);
	}
	else {
	  file = NetcdfFile.open(m_URL);
	}
	// TODO
      } catch (IOException ioe) {
        // just re-throw it
        throw ioe;
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    return new Instances(m_structure, 0);
  }

  /**
   * Return the full data set. If the structure hasn't yet been determined by a
   * call to getStructure then method should do so before processing the rest of
   * the data set.
   *
   * @return the structure of the data set as an empty set of Instances
   * @throws java.io.IOException if there is no source or parsing fails
   */
  @Override
  public Instances getDataSet() throws IOException {
    if ((m_File == null) && (m_URL == null)) {
      throw new IOException("No source (file/URL) has been specified");
    }

    if (getRetrieval() == INCREMENTAL) {
      throw new IOException(
        "Cannot mix getting Instances in both incremental and batch modes");
    }

    setRetrieval(BATCH);
    if (m_structure == null) {
      getStructure();
    }

    Instances result = null;

    try {
      // TODO
    } catch (Exception ex) {
      System.err.println("Failed to load NetCDF file");
      ex.printStackTrace();
    }

    return result;
  }

  /**
   * JSONLoader is unable to process a data set incrementally.
   *
   * @param structure ignored
   * @return never returns without throwing an exception
   * @throws java.io.IOException always. JSONLoader is unable to process a data set
   *           incrementally.
   */
  @Override
  public Instance getNextInstance(Instances structure) throws IOException {
    throw new IOException("NetCDFLoader can't read data sets incrementally.");
  }

  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 11573 $");
  }

  /**
   * Main method.
   * 
   * @param args should contain the name of an input file.
   */
  public static void main(String[] args) {
    runFileLoader(new NetCDFLoader(), args);
  }
}
