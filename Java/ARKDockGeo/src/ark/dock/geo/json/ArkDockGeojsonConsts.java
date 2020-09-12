package ark.dock.geo.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.json.simple.parser.ContentHandler;

public interface ArkDockGeojsonConsts {
    enum GeojsonKey {
        type, features, geometry, geometries, coordinates, properties, bbox, NULL
    }

    enum GeojsonType {
        Point, MultiPoint(true, Point), 
        LineString(false, Point), MultiLineString(true, LineString), 
        Polygon(false, LineString), MultiPolygon(true, Polygon), GeometryCollection(true, null, GeojsonKey.geometries), 
        Feature(false), FeatureCollection(true, Feature, GeojsonKey.features), 
        NULL;

        public final boolean container;
        public final GeojsonType childType;
        public final String childKey;

        private GeojsonType(boolean container, GeojsonType childType, GeojsonKey childKey) {
            this.container = container;
            this.childType = childType;
            this.childKey = (null == childKey) ? null : childKey.name();
        }
        private GeojsonType(boolean container, GeojsonType childType) {
            this(container, childType, GeojsonKey.coordinates);
        }
        private GeojsonType(GeojsonType childType) {
            this(false, childType, null);
        }
        private GeojsonType(boolean container) {
            this(container, null, null);
        }
        private GeojsonType() {
            this(false, null, null);
        }
        
        public boolean isArrKey(String key) {
            return key.equals(childKey);
        }
    };

    abstract class GeojsonBuilder {
        protected ContentHandler extHandler;
        protected GeojsonType currType;
        protected Object currObj;
        
        public void select(GeojsonType gjt, Object geoObj) {
            currType = gjt;
            currObj = geoObj;
        }
        
        public Object newBBox(Collection<?> points) {
            return null;
        }

        public boolean setBBox(Object bb) {
            if (currObj instanceof GeojsonObjectContainer) {
                ((GeojsonObjectContainer) currObj).setBBox(bb);
                return true;
            }
            return false;
        }

        public boolean addChild(Object data, int idx) {
            if (currObj instanceof GeojsonObjectContainer) {
                ((GeojsonObjectContainer) currObj).addChild(data);
                return true;
            }
            return false;
        }

        public Object newGeojsonObj(GeojsonType gjt) {
            return gjt.container ? new GeojsonObjectArray(gjt) : null;
        }
        
        public ContentHandler getExtHandler() {
            return extHandler;
        }
    }

    public interface GeojsonObjectContainer {
        boolean addChild(Object data);
        void setBBox(Object bb);
        Object getBbox();
    }

    public class GeojsonObjectFeature extends HashMap<String, Object> implements GeojsonObjectContainer {
        private static final long serialVersionUID = 1L;

        @SuppressWarnings("unchecked")
        @Override
        public boolean addChild(Object data) {
            SimpleEntry<String, Object> e = (SimpleEntry<String, Object>) data;
            put(e.getKey(), e.getValue());
            return true;
        }

        @Override
        public void setBBox(Object bb) {
            put("bbox", bb);
        }

        @Override
        public Object getBbox() {
            return get("bbox");
        }
    }

    public class GeojsonObjectArray extends ArrayList<Object> implements GeojsonObjectContainer {
        private static final long serialVersionUID = 1L;

        private final GeojsonType type;
        protected Object bbox;

        GeojsonObjectArray(GeojsonType t) {
            this.type = t;
        }

        public GeojsonType getType() {
            return type;
        }

        public void setBBox(Object bb) {
            this.bbox = bb;
        }

        public Object getBbox() {
            return bbox;
        }
        
        @Override
        public boolean addChild(Object data) {
            add(data);
            return true;
        }
    }
    
    public class GeojsonPolygon<NativeLineRing> implements GeojsonObjectContainer {
        protected Object bbox;
        protected NativeLineRing exterior;
        protected ArrayList<NativeLineRing> holes;

        public GeojsonType getType() {
            return GeojsonType.Polygon;
        }

        public void setBBox(Object bb) {
            this.bbox = bb;
        }

        public Object getBbox() {
            return bbox;
        }
        
        @SuppressWarnings("unchecked")
        @Override
        public boolean addChild(Object data) {
            NativeLineRing lr = (NativeLineRing) data;
            if ( null == exterior ) {
                exterior = lr;
            } else {
                getHoles().add(lr);
            }
            return true;
        }
        
        public NativeLineRing getExterior() {
            return exterior;
        }
        
        public ArrayList<NativeLineRing> getHoles() {
            if ( null == holes ) {
                holes = new ArrayList<>();
            }
            return holes;
        }
    }
}
