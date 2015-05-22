/**
 * 
 */
package org.fenggui.theme.xml;

/**
 * @author Esa Tanskanen
 *
 */
public class LowerCaseEnumFormat<T extends Enum> extends EnumFormat<T>
{
	/**
	 * @param enumClass
	 */
	public LowerCaseEnumFormat(Class<T> enumClass)
	{
		super(enumClass);
	}


	/* (non-Javadoc)
	 * @see org.fenggui.io.EnumFormat#encodeName(java.lang.String)
	 */
	@Override
	protected String encodeName(String name) throws EncodingException
	{
		name = name.toLowerCase();
		if (!name.contains("-"))
		{
			name = name.replace('_', '-');
		}
		else if (!name.contains("_")) { throw new EncodingException("The encoded name '" + name
				+ "' of the enumeration " + _enumClass.getName() + " would be ambiguous"); }

		return name;
	}


	/* (non-Javadoc)
	 * @see org.fenggui.io.EnumFormat#decodeName(java.lang.String)
	 */
	@Override
	protected String decodeName(String encodedName)
	{
		if (!encodedName.contains("_"))
		{
			encodedName = encodedName.replace('-', '_');
		}

		return encodedName;
	}


	@Override
	protected boolean equalityOperation(String enumName, String decodedName)
	{
		return enumName.equalsIgnoreCase(decodedName);
	}
}
