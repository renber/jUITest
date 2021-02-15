package de.renebergelt.juitest.monitor.viewmodels;

import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.xbindings.PropertyChangeSupportBase;

import java.lang.reflect.Field;
import java.util.HashMap;

public abstract class ViewModelBase extends PropertyChangeSupportBase {
    HashMap<String, PropertyCacheItem> propertyCache = new HashMap();

    public ViewModelBase() {
    }

    protected boolean changeProperty(String propertyName, Object newValue) {
        return this.changeProperty(this, propertyName, propertyName, newValue);
    }

    protected boolean changeProperty(Object model, String propertyName, Object newValue) {
        return this.changeProperty(model, propertyName, propertyName, newValue);
    }

    protected boolean changeProperty(Object model, String fieldName, String propertyName, Object newValue) {
        try {
            ViewModelBase.PropertyCacheItem pc = (ViewModelBase.PropertyCacheItem)this.propertyCache.get(model.getClass().getName() + "." + fieldName);
            if (pc == null) {
                pc = new ViewModelBase.PropertyCacheItem();
                pc.getter = BeanProperty.create(propertyName);
                pc.setter = getFieldByName(model.getClass(), fieldName);
                pc.setter.setAccessible(true);
                this.propertyCache.put(model.getClass().getName() + "." + fieldName, pc);
            }

            Object oldValue = pc.getter.getValue(this);
            if (oldValue != newValue && (oldValue == null || !oldValue.equals(newValue))) {
                pc.setter.set(model, newValue);
                this.firePropertyChanged(propertyName, oldValue, newValue);
                return true;
            } else {
                return false;
            }
        } catch (Exception var7) {
            throw new RuntimeException("The property '" + propertyName + "' could not be changed.", var7);
        }
    }

    private static Field getFieldByName(Class<?> type, String fieldName) throws NoSuchFieldException {
        for(Class c = type; c != null; c = c.getSuperclass()) {
            Field[] var3 = c.getDeclaredFields();
            int var4 = var3.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                Field f = var3[var5];
                if (f.getName().equals(fieldName)) {
                    return f;
                }
            }
        }

        throw new NoSuchFieldException(fieldName);
    }

    class PropertyCacheItem {
        public BeanProperty getter;
        public Field setter;

        PropertyCacheItem() {
        }
    }
}
