package app.com.getplace.Objects;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by DorSh on 22-Jan-17.
 */

public class PlaceTypeModule {
    public List<PlaceTypeModule> getPlaceTypes;

    public PlaceTypeModule(List<PlaceTypeModule> getPlaceTypes) {
        this.getPlaceTypes = getPlaceTypes;
    }


    public List<PlaceTypeModule> getGetPlaceTypes() {
        return getPlaceTypes;
    }

    public void setGetPlaceTypes(List<PlaceTypeModule> getPlaceTypes) {
        this.getPlaceTypes = getPlaceTypes;
    }
}
