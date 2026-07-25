# GetTicket – שרת (Application Tier)

מערכת הזמנת כרטיסים בארכיטקטורת 3 שכבות (JSF + Java + MySQL). ריפו זה מכיל את **רובד השרת (Application Tier)** של הפרויקט.

## חלוקת עבודה בצוות

הפרויקט מחולק לשלושה רבדים, כל אחד באחריות חבר/ת צוות אחר:

1. **רובד הלקוח (Client Tier)** – עיצוב מסכים (Wireframing), כתיבת XHTML/CSS סטטי, Mock Data קשיח ב-JSF.
2. **רובד השרת (Application Tier)** – **הריפו הזה**: Models, Services (לוגיקה עסקית), JSF Backing Beans.
3. **רובד בסיס הנתונים (Data Tier)** – סכמת SQL לפי ה-ERD, נתוני דמו, Connection Pool, DAO.

## מצב נוכחי – חשוב לקרוא!

הקוד כאן כולל **גם** מימוש מלא של שכבת ה-DAO, ConnectionPool, וקבצי XHTML – אף שאלה טכנית שייכים לרבדים האחרים. זה נעשה כדי לאפשר לבדוק את שכבת ה-Service מקצה לקצה כבר עכשיו, ולתת נקודת פתיחה לשאר הצוות.

**חשוב**: אלו **טיוטות ייחוס בלבד**, לא מימוש סופי מחייב:

- ה-DAO וה-`ConnectionPool` מניחים סכמת טבלאות/עמודות זהה למסמך התכנון (`Location_id`, `Uid` וכו'). מפתח/ת בסיס הנתונים צריכ/ה לוודא התאמה מלאה מול הסכמה בפועל, או לעדכן את ה-SQL בהתאם.
- קבצי ה-XHTML הם דוגמה עובדת (מתחברים ל-Backing Beans אמיתיים), לא Wireframe סופי – מפתח/ת הלקוח חופשי/ה לעצב ולבנות מחדש.

## מבנה הפרויקט

```
src/main/java/getticket/
├── model/    – POJOs: Location, User, Show, Venue, Seat, EventInstance, Booking, Ticket, Review
├── dao/      – ממשקי DAO + מימושים ב-JDBC (dao/impl), כולם עם PreparedStatement
├── service/  – לוגיקה עסקית: BookingService (הזמנה בטרנזקציה אטומית, מניעת Double Booking)
├── util/     – ConnectionPool, PasswordUtil (hash לסיסמאות)
└── web/      – JSF Backing Beans: UserSessionBean, CatalogBean, CheckoutBean, AuthFilter

src/main/webapp/          – קבצי XHTML + web.xml / faces-config.xml
src/main/resources/db.properties – פרטי חיבור ל-DB (ניתן לדרוס עם משתני סביבה)
```

## הרצה מקומית

דרישות: Java 17, Maven, MySQL, Tomcat 9.

1. הקימו סכמת MySQL לפי מסמך התכנון (טבלאות: Locations, Users, Shows, Venues, Seats, Event_Instances, Bookings, Tickets, Reviews).
2. הגדירו פרטי חיבור דרך משתני סביבה (`DB_URL`, `DB_USER`, `DB_PASSWORD`) או ערכו את `src/main/resources/db.properties`.
3. `mvn clean package` ופרסו את קובץ ה-WAR שנוצר ל-Tomcat.
