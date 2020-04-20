package fi.hsl.transitlog.hfp.persisthfpdata;

import uk.co.jemos.podam.api.*;
import uk.co.jemos.podam.typeManufacturers.*;

import java.sql.*;

public abstract class AbstractPodamTest {

    protected final PodamFactoryImpl podamFactory;

    public AbstractPodamTest() {
        TypeManufacturer<Integer> manufacturer = new IntTypeManufacturerImpl() {

            @Override
            public Integer getInteger(AttributeMetadata attributeMetadata) {
                if (attributeMetadata.getPojoClass() == Timestamp.class) {
                    return PodamUtils.getIntegerInRange(0, 100);
                } else {
                    return super.getInteger(attributeMetadata);
                }
            }
        };

        podamFactory = new PodamFactoryImpl();
        podamFactory.getStrategy().addOrReplaceTypeManufacturer(int.class, manufacturer);
    }

}
