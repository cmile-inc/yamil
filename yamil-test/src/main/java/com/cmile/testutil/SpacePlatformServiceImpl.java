package com.cmile.testutil;

import com.cmile.serviceutil.validators.space.SpaceDetails;
import com.cmile.serviceutil.validators.space.SpacePlatformService;

public class SpacePlatformServiceImpl implements SpacePlatformService {

  @Override
  public SpaceDetails getSpaceDetails(String spaceId) {
    SpaceDetails spaceDetails = new SpaceDetails();
    spaceDetails.setSpaceId("test-space");
    return spaceDetails;
  }
}
