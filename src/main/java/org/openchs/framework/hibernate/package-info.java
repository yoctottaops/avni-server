@org.hibernate.annotations.TypeDefs({
        @org.hibernate.annotations.TypeDef(name = "observations", typeClass = ObservationCollectionUserType.class),
        @org.hibernate.annotations.TypeDef(name = "keyValues", typeClass = KeyValuesUserType.class)
})

package org.openchs.framework.hibernate;