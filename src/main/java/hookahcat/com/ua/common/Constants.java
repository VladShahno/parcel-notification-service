package hookahcat.com.ua.common;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    public static final int WEB_CLIENT_BUFFER_SIZE = 16 * 1024 * 1024;

    @UtilityClass
    public class CalledMethods {

        public static final String GET_STATUS_DOCUMENTS = "getStatusDocuments";
        public static final String GET_DOCUMENT_LIST = "getDocumentList";
    }

    @UtilityClass
    public class ModelsName {

        public static final String TRACKING_DOCUMENT = "TrackingDocument";
        public static final String INTERNET_DOCUMENT = "InternetDocument";
    }
}
