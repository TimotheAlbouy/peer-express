package fr.ensibs.photosharing;

import java.util.Properties;
import java.io.File;
import java.io.Serializable;

/**
* A photo shared by a user composed of a file and tags that describe the photo
*/
public interface Photo extends Serializable
{

  /**
  * Give the photo tags that describe the photo
  *
  * @return the photo tags
  */
  Properties getTags();

  /**
  * Give the photo binary file that contains the photo
  *
  * @return the photo file
  */
  File getFile();

  /**
  * Give the user that shared the photo, considered as the photo owner
  *
  * @return the photo owner
  */
  String getOwner();
}
