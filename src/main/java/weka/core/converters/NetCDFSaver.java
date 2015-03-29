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
 * NetCDFSaver.java
 * Copyright (C) 2015 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.core.converters;

import ucar.ma2.Array;
import ucar.ma2.ArrayChar;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.RevisionUtils;
import weka.core.Utils;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 <!-- globalinfo-end -->
 * 
 <!-- options-start -->
 <!-- options-end -->
 *
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 10390 $
 * @see weka.core.converters.Saver
 */
public class NetCDFSaver extends AbstractFileSaver implements BatchConverter {

  /** for serialization. */
  private static final long serialVersionUID = -7446832500561589653L;

  /** the date format string. */
  public final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

  /** whether to save date variables as LONG instead of STRING. */
  protected boolean m_DateAsLong = false;

  /** the maximum length for strings. */
  protected int m_MaxLenString = 255;

  /**
   * Constructor.
   */
  public NetCDFSaver() {
    resetOptions();
  }

  /**
   * Returns a string describing this Saver.
   *
   * @return a description of the Saver suitable for displaying in the
   *         explorer/experimenter gui
   */
  public String globalInfo() {
    return
      "Writes the data to NetCDF files.\n"
      + "Dates are either stored as STRING (format: " + DATE_FORMAT + ") "
      + "or as LONG (Java epoch, msec since 1970).\n"
      + "Uses '?' for missing values in strings and NaN for numeric values.";
  }

  /**
   * Resets the Saver.
   */
  @Override
  public void resetOptions() {
    super.resetOptions();
    setFileExtension(NetCDFLoader.FILE_EXTENSION_CDF);
  }

  /**
   * Returns a description of the file type.
   *
   * @return a short file description
   */
  @Override
  public String getFileDescription() {
    return NetCDFLoader.FILE_DESCRIPTION;
  }

  /**
   * Gets all the file extensions used for this type of file.
   *
   * @return the file extensions
   */
  @Override
  public String[] getFileExtensions() {
    return new String[]{
      NetCDFLoader.FILE_EXTENSION_NC,
      NetCDFLoader.FILE_EXTENSION_CDF
    };
  }

  /**
   * Gets an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {
    Vector result;
    Enumeration	enm;

    result = new Vector();

    enm = super.listOptions();
    while (enm.hasMoreElements())
      result.add(enm.nextElement());

    result.addElement(new Option(
        "\tWhether to save date variables as LONG.\n"
        + "\t(default: no)",
        "date-as-long", 0, "-date-as-long"));

    result.addElement(new Option(
        "\tThe maximum length for strings.\n"
        + "\t(default: 255)",
        "max-len-string", 1, "-max-len-string <num>"));

    return result.elements();
  }

  /**
   * returns the options of the current setup.
   *
   * @return		the current options
   */
  public String[] getOptions() {
    Vector<String>	result;
    String[]		options;
    int			i;

    result = new Vector<String>();

    options = super.getOptions();
    for (i = 0; i < options.length; i++)
      result.add(options[i]);

    if (getDateAsLong())
      result.add("-date-as-long");

    result.add("-max-len-string");
    result.add("" + getMaxLenString());

    return result.toArray(new String[result.size()]);
  }

  /**
   * Parses the options for this object.
   *
   * @param options	the options to use
   * @throws Exception	if setting of options fails
   */
  public void setOptions(String[] options) throws Exception {
    String	tmpStr;

    setDateAsLong(Utils.getFlag("date-as-long", options));

    tmpStr = Utils.getOption("max-len-string", options);
    if (tmpStr.length() != 0)
      setMaxLenString(Integer.parseInt(tmpStr));
    else
      setMaxLenString(255);

    super.setOptions(options);
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String dateAsLongTipText() {
    return "If enabled, dates get saved as LONG instead of STRING.";
  }

  /**
   * Get whether to save dates as LONG instead of STRING.
   *
   * @return true if saved as LONG.
   */
  public boolean getDateAsLong() {
    return m_DateAsLong;
  }

  /**
   * Set whether to save dates as LONG instead of STRING.
   *
   * @param value true if saved as LONG.
   */
  public void setDateAsLong(boolean value) {
    m_DateAsLong = value;
  }

  /**
   * Returns the tip text for this property.
   *
   * @return 		tip text for this property suitable for
   * 			displaying in the explorer/experimenter gui
   */
  public String maxTipText() {
    return "The maximum.";
  }

  /**
   * Get the maximum length for strings.
   *
   * @return the maximum lengt.
   */
  public int getMaxLenString() {
    return m_MaxLenString;
  }

  /**
   * Set the maximum length for strings.
   *
   * @param value the maximum length.
   */
  public void setMaxLenString(int value) {
    m_MaxLenString = value;
  }

  /**
   * Returns the Capabilities of this saver.
   *
   * @return the capabilities of this object
   * @see weka.core.Capabilities
   */
  @Override
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();

    // attributes
    result.enable(Capability.NOMINAL_ATTRIBUTES);
    result.enable(Capability.NUMERIC_ATTRIBUTES);
    result.enable(Capability.DATE_ATTRIBUTES);
    result.enable(Capability.STRING_ATTRIBUTES);
    result.enable(Capability.MISSING_VALUES);

    // class
    result.enable(Capability.NOMINAL_CLASS);
    result.enable(Capability.NUMERIC_CLASS);
    result.enable(Capability.DATE_CLASS);
    result.enable(Capability.STRING_CLASS);
    result.enable(Capability.MISSING_CLASS_VALUES);
    result.enable(Capability.NO_CLASS);

    return result;
  }

  /**
   * Creates a valid attribute name.
   *
   * @param name the name to process
   * @return the "fixed" name
   */
  protected String makeValidName(String name) {
    StringBuilder result;
    int i;
    char c;

    result = new StringBuilder();
    for (i = 0; i < name.length(); i++) {
      c = name.charAt(i);
      if ((c <= 32) || (c == 47))
        c = '_';
      result.append(c);
    }

    return result.toString();
  }

  /**
   * Writes a Batch of instances.
   *
   * @throws java.io.IOException throws IOException if saving in batch mode is not
   *           possible
   */
  @Override
  public void writeBatch() throws IOException {
    if (getInstances() == null) {
      throw new IOException("No instances to save");
    }

    if (retrieveFile() == null) {
      throw new IOException("No output file set");
    }

    if (getRetrieval() == INCREMENTAL) {
      throw new IOException("Batch and incremental saving cannot be mixed.");
    }

    setRetrieval(BATCH);
    setWriteMode(WRITE);

    Instances data = getInstances();
    try {
      NetcdfFileWriter writer = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, retrieveFile().getAbsolutePath());

      // create header
      Variable var[] = new Variable[data.numAttributes()];
      int[] maxLengths = new int[data.numAttributes()];
      // generic string length dimension/var
      writer.addDimension(null, "str_len", m_MaxLenString);
      writer.addVariable(null, "str", DataType.CHAR, "str_len");
      writer.addDimension(null, "num_instances", data.numInstances());
      for (int i = 0; i < data.numAttributes(); i++) {
	Attribute att = data.attribute(i);
        String name = makeValidName(att.name());
        maxLengths[i] = -1;
	switch (att.type()) {
          case Attribute.NUMERIC:
	    var[i] = writer.addVariable(null, name, DataType.DOUBLE, "num_instances");
	    break;
	  case Attribute.DATE:
	    if (m_DateAsLong) {
	      var[i] = writer.addVariable(null, name, DataType.LONG, "num_instances");
            }
	    else {
              writer.addDimension(null, name + "_len", DATE_FORMAT.length());
              var[i] = writer.addVariable(null, name, DataType.CHAR, "num_instances" + " " + name + "_len");
              maxLengths[i] = DATE_FORMAT.length();
	    }
	    break;
	  case Attribute.NOMINAL:
	    int maxLen = 0;
	    for (int n = 0; n < att.numValues(); n++)
	      maxLen = Math.max(maxLen, att.value(n).length());
            writer.addDimension(null, name + "_len", maxLen);
            var[i] = writer.addVariable(null, name, DataType.CHAR, "num_instances" + " " + name + "_len");
            maxLengths[i] = maxLen;
	    break;
	  case Attribute.STRING:
	    var[i] = writer.addVariable(null, name, DataType.CHAR, "num_instances" + " str_len");
            maxLengths[i] = m_MaxLenString;
	    break;
	  default:
	    throw new IllegalStateException("Unhandled attribute type: " + Attribute.typeToString(att.type()));
	}
      }
      writer.create();

      // add data
      Array array;
      ArrayChar arrayChar;
      Index index;
      SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
      for (int i = 0; i < data.numAttributes(); i++) {
	Attribute att = data.attribute(i);
        int[] shape = var[i].getShape();
	switch (att.type()) {
	  case Attribute.NUMERIC:
	    array = Array.factory(DataType.DOUBLE, new int[]{shape[0]});
	    for (int n = 0; n < data.numInstances(); n++) {
              Instance inst = data.instance(n);
              if (inst.isMissing(i))
                array.setDouble(n, Double.NaN);
              else
                array.setDouble(n, inst.value(i));
            }
	    writer.write(var[i], array);
	    break;
	  case Attribute.DATE:
	    if (m_DateAsLong) {
	      array = Array.factory(DataType.LONG, new int[]{shape[0]});
	      for (int n = 0; n < data.numInstances(); n++) {
                Instance inst = data.instance(n);
                if (inst.isMissing(i))
                  array.setLong(n, Long.MIN_VALUE);
                else
                  array.setLong(n, (long) inst.value(i));
              }
	      writer.write(var[i], array);
	    }
	    else {
              arrayChar = new ArrayChar.D2(shape[0], shape[1]);
              index = arrayChar.getIndex();
              for (int n = 0; n < data.numInstances(); n++) {
                Instance inst = data.instance(n);
                Date date = new Date((long) inst.value(i));
                if (inst.isMissing(i))
                  arrayChar.setString(index.set(n), "?");
                else
                  arrayChar.setString(index.set(n), df.format(date));
              }
              writer.write(var[i], arrayChar);
            }
	    break;
	  case Attribute.NOMINAL:
	  case Attribute.STRING:
	    arrayChar = new ArrayChar.D2(shape[0], shape[1]);
            index = arrayChar.getIndex();
	    for (int n = 0; n < data.numInstances(); n++) {
              Instance inst = data.instance(n);
              if (inst.isMissing(i))
                arrayChar.setString(index.set(n), "?");
              else
                arrayChar.setString(index.set(n), inst.stringValue(i));
            }
            writer.write(var[i], arrayChar);
	    break;
	  default:
	    throw new IllegalStateException("Unhandled attribute type: " + Attribute.typeToString(att.type()));
	}
      }
      writer.close();
    } catch (Exception e) {
      throw new IOException(e);
    }

    setWriteMode(WAIT);
    resetWriter();
    setWriteMode(CANCEL);
  }

  /**
   * Returns the revision string.
   * 
   * @return the revision
   */
  @Override
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 10390 $");
  }

  /**
   * Main method.
   * 
   * @param args should contain the options of a Saver.
   */
  public static void main(String[] args) {
    runFileSaver(new NetCDFSaver(), args);
  }
}
