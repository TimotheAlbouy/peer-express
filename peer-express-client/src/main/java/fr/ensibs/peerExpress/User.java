package fr.ensibs.peerExpress;

import java.io.File;
import java.util.Properties;
import java.util.List;

/**
* A user of the photo sharing application
*/
public interface User
{
  /**
  * Give the user name
  *
  * @return the user name
  */
  String getName();

  /**
  * Give the local directory where user photos are stored
  *
  * @return the user directory
  */
  File getDirectory();

  /**
  * Give the photos shared by the user
  *
  * @return the shared photos of the user
  */
  List<Photo> getSharedPhotos();

  /**
  * Give the photos received by the user
  *
  * @return the received photos of the user
  */
  List<Photo> getReceivedPhotos();

  /**
  * Share a new photo
  *
  * @param photo the new photo to be shared
  * @return true if the photo has been successfully shared
  */
  boolean share(Photo photo);

  /**
  * Receive a new photo
  *
  * @param photo the new photo that has been received
  */
  void receive(Photo photo);

  /**
  * Set new tags that specify the photos the user is interested in
  *
  * @param tags the new tags
  * @return true if the filter has been successfully set
  */
  boolean setFilter(Properties tags);
}
