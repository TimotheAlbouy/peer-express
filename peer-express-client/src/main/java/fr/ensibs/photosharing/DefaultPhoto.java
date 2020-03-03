package fr.ensibs.photosharing;

import java.util.Properties;
import java.io.File;

/**
* A photo shared by a user composed of a file and tags that describe the photo
*/
public class DefaultPhoto implements Photo
{

  /**
  * the photo tags that describe the photo
  */
  private Properties tags;

  /**
  * the photo binary file that contains the photo
  */
  private File file;

  /**
  * the user that shared the photo, considered as the photo owner
  */
  private String owner;

  /**
  * Constructor
  *
  * @param file the photo binary file
  * @param tags the photo tags
  * @param owner the user that shared the photo
  */
  public DefaultPhoto(File file, Properties tags, String owner)
  {
    this.file = file;
    this.tags = tags;
    this.owner = owner;
  }

  @Override
  public Properties getTags()
  {
    return this.tags;
  }

  @Override
  public File getFile()
  {
    return this.file;
  }

  @Override
  public String getOwner()
  {
    return this.owner;
  }

  @Override
  public String toString()
  {
    return "{ Photo: file=" + file + ",owner=" + owner + ",tags=" + tags + " }";
  }
}
