# Parcel notification service

This service is used to get parcel data via novaposhta-api.

**ParcelSearchingJob** - once a day retrieves all parcels for the last month, filters by the
statuses "Прибув на відділення" or "Прибув на відділення (завантажено в Поштомат)" and by the
parameter ActualDeliveryDate of the parcel at the post office, if more than **four days** have
passed, then this product will be added to csv file and sent as a message to the email.
Through the following environment variables, the following parameters can be configured:
**_novaposhta-api_**
- API_BASE_URL (base url for novaposhta-api)
- API_KEY (required for API calls)
- SENDER_PHONE_NUMBER (required for API calls)
**_csv_**
- CSV_EXPORT_RESPONSE_HEADERS (headers in a csv file)
- CSV_EXPORT_RESPONSE_FIELDS (entity fields from DocumentDataResponse)
**_SMTP_**
- SMTP_PASSWORD (password for sender mailbox)
- SMTP_LOGIN (email (login) for sender mailbox)
**_email-notification_** (sending email info)
- EMAIL_SUBJECT
- EMAIL_RECIPIENT
- EMAIL_SENDER
- EMAIL_MESSAGE
**_parcel-searching-job_**
- PARCEL_SEARCH_JOB - scheduled cron time for searching parcels

# Swagger
dev Swagger url can be found via this url:
http://localhost:8080/parcel-notification-service/swagger-ui/index.html#/