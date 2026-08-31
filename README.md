# ReserveIt / GetTicket – מערכת הזמנת כרטיסים

מערכת הזמנת כרטיסים בארכיטקטורת שלושה רבדים (JSF + Java + MySQL), שנבנתה כפרויקט סדנה בתכנות מתקדם ב-Java.

## מה יש בריפו

| תיקייה | מה זה | ארטיפקט |
|---|---|---|
| `src/` | **רובד השרת (Application Tier)** – האפליקציה המלאה: Models, DAO/JDBC, Services, Backing Beans ומסכי XHTML, מול MySQL אמיתי | `get-ticket.war` |
| `client-tier/` | **רובד הלקוח (Client Tier)** – מוקאפ עובד מול `MockData` בזיכרון, ללא DB (פירוט ב-`client-tier/README.md`) | `get-ticket-client.war` |
| `database/` | **רובד בסיס הנתונים (Data Tier)** – `schema.sql` (סכמה מלאה עם מפתחות זרים ואילוצים) ו-`seed_data.sql` (נתוני דמו) | — |

## הרצה מקומית

דרישות: Java 17, Maven, MySQL 8, Tomcat 9.

**1. הקמת בסיס הנתונים**

```bash
mysql -u root -p < database/schema.sql
mysql -u root -p getticket < database/seed_data.sql
```

`schema.sql` יוצר את בסיס הנתונים `getticket` ומוחק טבלאות קיימות – אל תריצו אותו על DB עם נתונים אמיתיים.
`seed_data.sql` טוען קטלוג דמו: 4 ערים, 3 אולמות ממוספרים עם מפת מושבים, 11 הופעות ו-21 מופעים.

**2. פרטי חיבור**

ברירת המחדל נמצאת ב-`src/main/resources/db.properties`, וניתן לדרוס אותה במשתני סביבה: `DB_URL`, `DB_USER`, `DB_PASSWORD`.

**3. בנייה ופריסה**

```bash
mvn clean package
cp target/get-ticket.war $CATALINA_HOME/webapps/
```

האפליקציה תעלה בכתובת http://localhost:8080/get-ticket/

מוקאפ הלקוח נבנה בנפרד (`cd client-tier && mvn clean package`) ונפרס כ-`get-ticket-client.war`.

## משתמש ניהול

הסכמה נטענת עם חשבון מנהל שאפשר להתחבר איתו מיד:

| שם משתמש | סיסמה | הרשאה |
|---|---|---|
| `admin` | `admin123` | ADMIN |

זהו חשבון **דמו לפיתוח מקומי בלבד**, שה-hash שלו כתוב בתוך `seed_data.sql` – יש להחליף אותו בכל סביבה אמיתית.

שני המשתמשים `yossi` ו-`dana` הם לקוחות דמו לצורך הצגת הזמנות קיימות; ה-`Password` שלהם הוא מחרוזת placeholder ולא hash תקין, ולכן **לא ניתן להתחבר איתם**. משתמשים חדשים נרשמים דרך טופס ההרשמה שבמסך הכניסה, והסיסמה נשמרת כ-hash באמצעות `PasswordUtil` (PBKDF2WithHmacSHA256, 65536 סיבובים, salt אקראי).

## מסכים

**לקוח**: קטלוג עם חיפוש לפי שם/קטגוריה/תאריך ← פרטי הופעה ובחירת מופע ← בחירת מושבים (מפה גרפית לאולם ממוספר) ← אישור הזמנה.

**ניהול** (מוגן ב-`AdminFilter`, נגיש רק ל-ADMIN):

- **אולמות** – הקמה ועריכה של אולמות; יצירת אולם ממוספר פורשת אוטומטית את מפת המושבים דרך `VenueService`.
- **הופעות ומופעים** – ניהול הופעות, ולכל הופעה לוח מופעים (אולם, מועד, מחיר, כמות כרטיסים, סטטוס).
- **מעקב הזמנות** – כל ההזמנות במערכת ועדכון סטטוס.
- **דוחות מכירות** – סינון לפי טווח תאריכים וסטטוס, עם סיכומי הכנסות וכרטיסים ופילוח לפי הופעה, אולם וסטטוס.

## מבנה הפרויקט

```
src/main/java/getticket/
├── model/    – POJOs: Location, User, Show, Venue, Seat, EventInstance, Booking, Ticket, Review
├── dao/      – ממשקי DAO + מימושים ב-JDBC (dao/impl), כולם עם PreparedStatement
├── service/  – לוגיקה עסקית: BookingService (הזמנה בטרנזקציה אטומית, מניעת Double Booking),
│               VenueService (אולם + מפת מושבים), OrderReportService (נתוני ההזמנות והדוחות)
├── util/     – ConnectionPool, PasswordUtil
└── web/      – Backing Beans (Catalog, Checkout, UserSession, Admin*) + AuthFilter/AdminFilter

src/main/webapp/          – מסכי XHTML, תבנית משותפת ו-CSS
database/                 – schema.sql + seed_data.sql
```

## הערות טכניות

- **JSF 2.2 (Mojarra)** ולא 2.3: Tomcat הוא Servlet Container בלבד ללא CDI, ו-Mojarra 2.3 נופל שם על `Unable to find CDI BeanManager`. כפועל יוצא אין תמיכה מובנית ב-`f:convertDateTime` עבור `java.time` – תאריכים מומרים ידנית ב-Backing Beans עם `DateTimeFormatter`.
- **מניעת Double Booking** נאכפת בבסיס הנתונים עצמו, באילוץ `UNIQUE (Instance_id, Seat_id)` על טבלת `Tickets`, ולא רק בקוד.
- **זמני מופע נשמרים כשעון קיר**: העמודות `Start_time` ו-`Booking_time` ממופות ישירות ל-`LocalDateTime` (ולא דרך `java.sql.Timestamp`), כדי שהדרייבר לא יבצע המרת אזור זמן בין ה-DB לשרת והמסכים יציגו בדיוק את מה ששמור.
- **מחירים מוצגים בשקלים (₪).**
