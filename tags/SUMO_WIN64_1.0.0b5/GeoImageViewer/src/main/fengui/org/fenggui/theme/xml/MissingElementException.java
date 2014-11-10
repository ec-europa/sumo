/**
 * 
 */
package org.fenggui.theme.xml;

/**
 * @author Esa Tanskanen
 *
 */
@SuppressWarnings("serial")
public class MissingElementException extends IXMLStreamableException
{
	
	public MissingElementException(String message)
	{
		super(message);
	}


	public MissingElementException(String message, Throwable cause)
	{
		super(message, cause);
	}


	public MissingElementException(Throwable cause)
	{
		super(cause);
	}
	
	/**
	 * Returns an MissingElementException with a suitable error message.
	 * 
	 * @param attribute the name of the missing element
	 * @return an MissingElementException with a suitable message
	 */
	public static MissingElementException createDefault(Iterable<?> names, String parsingContext, String nameList)
	{
		String namesStr = nameList;
		return new MissingElementException("required child element: " + namesStr + "\n\n" + parsingContext);
	}


	/**
	 * Returns an MalformedElementException with a suitable error message.
	 * 
	 * @param attribute the name of the malformed attribute
	 * @return an MissingElementException with a suitable message
	 */
	public static MalformedElementException createDefault(String name, String valueFormatDescrption, String parsingContext)
	{
		return new MalformedElementException("the attribute " + name + " should be " + valueFormatDescrption + "\n\n" + parsingContext);
	}	
}
