package ua.com.hookahcat.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final int WEB_CLIENT_BUFFER_SIZE = 16 * 1024 * 1024;
    public static final String ZERO = "0";
    public static final String ONE = "1";
    public static final String PAYMENT_METHOD_CASH = "Cash";
    public static final String ORDER_TYPE_CARGO_RETURN = "orderCargoReturn";
    public static final int MAX_STORAGE_DAYS_FOUR = 4;
    public static final int MAX_STORAGE_DAYS_NINE = 9;

    @UtilityClass
    public class CalledMethods {

        public static final String GET_STATUS_DOCUMENTS = "getStatusDocuments";
        public static final String GET_DOCUMENT_LIST = "getDocumentList";
        public static final String SAVE = "save";
    }

    @UtilityClass
    public class ModelsNames {

        public static final String TRACKING_DOCUMENT = "TrackingDocument";
        public static final String INTERNET_DOCUMENT = "InternetDocument";
        public static final String ADDITIONAL_SERVICE = "AdditionalService";
    }

    @UtilityClass
    public class StateNames {

        public static final String ARRIVED = "Прибув у відділення";
        public static final String ARRIVED_PARCEL_LOCKER = "Прибув на відділення (завантажено в Поштомат)";
    }

    @UtilityClass
    public class Patterns {

        public static final String DATE_PATTERN = "dd.MM.yyyy";
        public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    }
}
