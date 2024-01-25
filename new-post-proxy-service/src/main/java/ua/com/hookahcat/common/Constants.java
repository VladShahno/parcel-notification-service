package ua.com.hookahcat.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final int WEB_CLIENT_BUFFER_SIZE = 16 * 1024 * 1024;
    public static final String ZERO = "0";
    public static final String ONE = "1";
    public static final String PAYMENT_METHOD_CASH = "Cash";
    public static final String ORDER_TYPE_CARGO_RETURN = "orderCargoReturn";
    public static final String DOCUMENT_NUMBER = "documentNumber";
    public static final String PHONE_NUMBER = "phoneNumber";
    public static final String MAX_STORAGE_DAYS = "maxStorageDays";
    public static final String DATE_TIME_FROM = "dateTimeFrom";
    public static final String DATE_TIME_TO = "dateTimeTo";
    public static final String RETURN_ADDRESS_REF = "returnAddressRef";
    public static final String STATUS = "status";
    public static final String PAGE = "page";

    @UtilityClass
    public class CalledMethods {

        public static final String GET_STATUS_DOCUMENTS = "getStatusDocuments";
        public static final String GET_DOCUMENT_LIST = "getDocumentList";
        public static final String SAVE = "save";
        public static final String CHECK_POSSIBILITY_CREATE_RETURN = "CheckPossibilityCreateReturn";
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
    }

    @UtilityClass
    public class Patterns {

        public static final String DATE_PATTERN = "dd.MM.yyyy";
        public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    }

    @UtilityClass
    public class FileNames {

        public static final String NOT_RECEIVED_PARCELS = "Not received parcels [";
        public static final String RETURNED_PARCELS = "Returned parcels [";
    }
}
