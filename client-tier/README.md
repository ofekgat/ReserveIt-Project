# GetTicket – לקוח (Client Tier)

מערכת הזמנת כרטיסים בארכיטקטורת 3 שכבות (JSF + Java + MySQL). ריפו זה מכיל את **רובד הלקוח (Client Tier)** של הפרויקט.

## חלוקת עבודה בצוות

1. **רובד הלקוח (Client Tier)** – **הריפו הזה**: עיצוב מסכים, XHTML/CSS, Mock Data קשיח ב-JSF.
2. **רובד השרת (Application Tier)** – Models, Services (לוגיקה עסקית), JSF Backing Beans אמיתיים מול בסיס הנתונים.
3. **רובד בסיס הנתונים (Data Tier)** – סכמת SQL, נתוני דמו, Connection Pool, DAO.

## מצב נוכחי – חשוב לקרוא!

הריפו הזה הוא **מוקאפ עובד**, לא מימוש סופי: כל מסך רץ מול `MockData` — מחלקה יחידה, in-memory, ללא בסיס נתונים אמיתי — כדי לאפשר לעצב ולבדוק את הזרימה המלאה (עיון בהופעות → בחירת מושבים/כמות → הזמנה → אישור → "הכרטיסים שלי" → ביקורות) בלי לחכות לרובד השרת.

- מבנה העמודים, שמות ה-Backing Beans (`catalogBean`, `checkoutBean`, `userSessionBean`) ותבנית ה-CSS **תואמים בכוונה** לדוגמת העבודה ברובד השרת, כדי שכשה-DAO/Service האמיתיים יהיו מוכנים, אפשר יהיה להחליף את `getticket.client.mock.MockData` ב-DAO אמיתיים כמעט בלי לגעת ב-XHTML.
- שני מסכים **לא קיימים עדיין** ברובד השרת ונוספו כאן כטיוטה: `myBookings.xhtml` ("הכרטיסים שלי") ו-`reviews.xhtml` (ביקורות/דירוג להופעה).
- ההתחברות היא Mock בלבד: משתמש דמו `demo` / `demo123`, וכל הרשמה חדשה נשמרת רק בזיכרון (נמחקת באתחול השרת). **אין hashing אמיתי של סיסמאות כאן** — זה תפקיד רובד השרת; זו רק דוגמה ויזואלית.
- הזמנות/מושבים "נתפסים" רק בזיכרון של השרת הרץ, לא ב-DB. אין נעילות, אין טרנזקציות, אין הגנה מפני double booking במקביל — כל זה נמצא ב-`BookingService` האמיתי ברובד השרת.

## מבנה הפרויקט

```
src/main/java/getticket/client/
├── model/  – אותם POJOs שברובד השרת (Show, Venue, Seat, EventInstance, Booking, Ticket, User, Review)
├── mock/   – MockData: כל "בסיס הנתונים" של הלקוח, קשיח בזיכרון
└── web/    – JSF Backing Beans: UserSessionBean, CatalogBean, CheckoutBean, BookingHistoryBean, ReviewBean, AuthFilter

src/main/webapp/  – קבצי XHTML + web.xml / faces-config.xml, אותה תבנית ו-style.css כמו רובד השרת
```

## הרצה מקומית

דרישות: Java 17, Maven, Tomcat 9. **אין צורך ב-MySQL** — הכול Mock.

```
mvn clean package
```

ופרסו את ה-WAR שנוצר (`get-ticket-client.war`) ל-Tomcat. עמוד הפתיחה הוא `login.xhtml`.
