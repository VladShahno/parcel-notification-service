package ua.com.hookahcat.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final int WEB_CLIENT_BUFFER_SIZE = 16 * 1024 * 1024;
    public static final String ZERO = "0";
    public static final String ONE = "1";
    public static final int MAX_STORAGE_DAYS = 4;

    @UtilityClass
    public class CalledMethods {

        public static final String GET_STATUS_DOCUMENTS = "getStatusDocuments";
        public static final String GET_DOCUMENT_LIST = "getDocumentList";
    }

    @UtilityClass
    public class ModelsNames {

        public static final String TRACKING_DOCUMENT = "TrackingDocument";
        public static final String INTERNET_DOCUMENT = "InternetDocument";
    }

    @UtilityClass
    public class StateNames {

        public static final String ARRIVED = "Прибув на відділення";
        public static final String ARRIVED_PARCEL_LOCKER = "Прибув на відділення (завантажено в Поштомат)";
    }

    @UtilityClass
    public class Patterns {

        public static final String DATE_TIME_PATTERN = "dd.MM.yyyy";
    }
}
