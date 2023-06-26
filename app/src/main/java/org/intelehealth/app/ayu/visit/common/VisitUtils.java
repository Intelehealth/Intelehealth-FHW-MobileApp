package org.intelehealth.app.ayu.visit.common;

import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

public class VisitUtils {
    public static boolean checkNodeValidByGenderAndAge(String patientGender, float floatAgeYearMonth, String nodeGender, String minAge, String maxAge) {

        float minAgeF = minAge != null && !minAge.isEmpty() ? Float.parseFloat(minAge) : 0f;
        float maxAgeF = maxAge != null && !maxAge.isEmpty() ? Float.parseFloat(maxAge) : 0f;
        boolean isValidByGender = true;
        if (patientGender.equalsIgnoreCase("M") &&
                nodeGender.equalsIgnoreCase("0")) {

            isValidByGender = false;
        } else if (patientGender.equalsIgnoreCase("F") &&
                nodeGender.equalsIgnoreCase("1")) {
            isValidByGender = false;
        }

        if (isValidByGender) {
            if (minAgeF != 0f && maxAgeF != 0f) {
                isValidByGender = minAgeF <= floatAgeYearMonth && floatAgeYearMonth <= maxAgeF;
            } else if (minAgeF != 0f) {
                isValidByGender = floatAgeYearMonth >= minAgeF;
            } else if (maxAgeF != 0f) {
                isValidByGender = floatAgeYearMonth <= maxAgeF;
            }
        }
        return isValidByGender;
    }

    public static void scrollNow(RecyclerView recyclerView, long delayMills, int dx, int dy) {
        recyclerView.postDelayed(new Runnable() {
            @Override
            public void run() {

                recyclerView.smoothScrollBy(dx, dy);
            }
        }, delayMills);
    }

    public static String replaceEnglishCommonString(String data, String locale) {
        Log.v("VisitUtils", "RAW - "+data);
        String result = data;
        if (locale.equalsIgnoreCase("hi")) {

            result = result.replace("Question not answered", " सवाल का जवाब नहीं दिया")
                    .replace("Patient reports -", " पेशेंट ने सूचित किया -")
                    .replace("Patient denies -", " पेशेंट ने मना कर दिया -")
                    .replace("Hours", "घंटे").replace("Days", "दिन")
                    .replace("Weeks", "हफ्तों").replace("Months", "महीने")
                    .replace("Years", "वर्ष")
                    .replace("times per hour", "प्रति घंटे बार")
                    .replace("time per day", "प्रति दिन का समय")
                    .replace("times per week", "प्रति सप्ताह बार")
                    .replace("times per month", "प्रति माह बार")
                    .replace("times per year", "प्रति वर्ष बार");
        } else if (locale.equalsIgnoreCase("or")) {
            result = result.replace("Question not answered", "ପ୍ରଶ୍ନର ଉତ୍ତର ନାହିଁ |")
                    .replace("Patient reports -", " ରୋଗୀ ରିପୋର୍ଟ -")
                    .replace("Patient denies -", " ରୋଗୀ ଅସ୍ୱୀକାର କରନ୍ତି -")
                    .replace("Hours", "ଘଣ୍ଟା").replace("Days", "ଦିନ")
                    .replace("Weeks", "ସପ୍ତାହ").replace("Months", "ମାସ")
                    .replace("Years", "ବର୍ଷ")
                    .replace("times per hour", "ସମୟ ପ୍ରତି ଘଣ୍ଟା")
                    .replace("time per day", "ସମୟ ପ୍ରତିଦିନ")
                    .replace("times per week", "ସମୟ ପ୍ରତି ସପ୍ତାହ")
                    .replace("times per month", "ସମୟ ପ୍ରତି ମାସରେ |")
                    .replace("times per year", "ସମୟ ପ୍ରତିବର୍ଷ");
        } else if (locale.equalsIgnoreCase("gu")) {
            result = result.replace("Question not answered", "પ્રશ્નનો જવાબ મળ્યો નથી")
                    .replace("Patient reports -", "દરદી રિપોર્ટ કરે છે -")
                    .replace("Patient denies -", "દરદી મના કરે છે -")
                    .replace("Hours", "કલાક").replace("Days", "દિવસ")
                    .replace("Weeks", "અઠવાડિયું").replace("Months", "માસ")
                    .replace("Years", "વર્ષ")
                    .replace("times per hour", "કલાક દીઠ વખત")
                    .replace("time per day", "દિવસ દીઠ વખત")
                    .replace("times per week", "દર અઠવાડિયે વખત")
                    .replace("times per month", "દર મહિને વખત")
                    .replace("times per year", "વર્ષ દીઠ વખત");
        } else if (locale.equalsIgnoreCase("te")) {
            result = result.replace("Question not answered", "ప్రశ్నకు సమాధానం ఇవ్వలేదు")
                    .replace("Patient reports -", "రోగి నివేదికలు -")
                    .replace("Patient denies -", "రోగి నిరాకరించాడు -")
                    .replace("Hours", "గంటలు").replace("Days", "రోజులు")
                    .replace("Weeks", "వారాలు").replace("Months", "నెలల")
                    .replace("Years", "సంవత్సరాలు")
                    .replace("times per hour", "గంటకు సార్లు")
                    .replace("time per day", "రోజుకు సార్లు")
                    .replace("times per week", "వారానికి సార్లు")
                    .replace("times per month", "నెలకు సార్లు")
                    .replace("times per year", "సంవత్సరానికి సార్లు");
        } else if (locale.equalsIgnoreCase("mr")) {
            result = result.replace("Question not answered", "प्रश्नाचे उत्तर दिले नाही")
                    .replace("Patient reports -", "रुग्ण अहवाल-")
                    .replace("Patient denies -", "रुग्ण नकार देतो-")
                    .replace("Hours", "तास")
                    .replace("Days", "दिवस")
                    .replace("Weeks", "आठवडे")
                    .replace("Months", "महिने")
                    .replace("Years", "वर्षे")
                    .replace("times per hour", "प्रति तास")
                    .replace("time per day", "दररोज वेळा")
                    .replace("times per week", "आठवड्यातून काही वेळा")
                    .replace("times per month", "दरमहा वेळा")
                    .replace("times per year", "दरवर्षी वेळा");

        } else if (locale.equalsIgnoreCase("kn")) {
            result = result.replace("Question not answered", "ಪ್ರಶ್ನೆಗೆ ಉತ್ತರಿಸಲಾಗಿಲ್ಲ")
                    .replace("Patient reports -", "ರೋಗಿಯ ವರದಿಗಳು-")
                    .replace("Patient denies -", "ರೋಗಿಯು ನಿರಾಕರಿಸುತ್ತಾನೆ-")
                    .replace("Hours", "ಗಂಟೆಗಳು").replace("Days", "ದಿನಗಳು")
                    .replace("Weeks", "ವಾರಗಳು").replace("Months", "ತಿಂಗಳುಗಳು")
                    .replace("Years", "ವರ್ಷಗಳು")
                    .replace("times per hour", "ಗಂಟೆಗೆ ಬಾರಿ").replace("time per day", "ದಿನಕ್ಕೆ ಬಾರಿ")
                    .replace("times per week", "ವಾರಕ್ಕೆ ಬಾರಿ").replace("times per month", "ತಿಂಗಳಿಗೆ ಬಾರಿ")
                    .replace("times per year", "ವರ್ಷಕ್ಕೆ ಬಾರಿ");
        } else if (locale.equalsIgnoreCase("as")) {
            result = result.replace("Question not answered", "প্ৰশ্নৰ উত্তৰ দিয়া হোৱা নাই")
                    .replace("Patient reports -", "ৰোগীৰ প্ৰতিবেদন -")
                    .replace("Patient denies -", "ৰোগীয়ে অস্বীকাৰ কৰে -")
                    .replace("Hours", "ঘণ্টা").replace("Days", "দিনসমূহ")
                    .replace("Weeks", "সপ্তাহ").replace("Months", "মাহ")
                    .replace("Years", "বছৰ")
                    .replace("times per hour", "প্ৰতি ঘণ্টাত সময়")
                    .replace("time per day", "প্ৰতিদিনে সময়")
                    .replace("times per week", "প্ৰতি সপ্তাহত সময়")
                    .replace("times per month", "প্ৰতি মাহে সময়")
                    .replace("times per year", "প্ৰতি বছৰে সময়");
        }
        //Malyalam Language Support...
        else if (locale.equalsIgnoreCase("ml")) {
            result = result.replace("Question not answered", "ചോദ്യത്തിന് ഉത്തരം ലഭിച്ചില്ല")
                    .replace("Patient reports -", "രോഗിയുടെ റിപ്പോർട്ടുകൾ -")
                    .replace("Patient denies -", "രോഗി നിരസിക്കുന്നു -")
                    .replace("Hours", "മണിക്കൂറുകൾ").replace("Days", "ദിവസങ്ങളിൽ")
                    .replace("Weeks", "ആഴ്ചകൾ").replace("Months", "മാസങ്ങൾ")
                    .replace("Years", "വർഷങ്ങൾ")
                    .replace("times per hour", "മണിക്കൂറിൽ തവണ")
                    .replace("time per day", "പ്രതിദിനം തവണ")
                    .replace("times per week", "ആഴ്ചയിൽ തവണ")
                    .replace("times per month", "മാസത്തിൽ തവണ")
                    .replace("times per year", "വർഷത്തിൽ തവണ");
        } else if (locale.equalsIgnoreCase("bn")) {
            result = result.replace("Question not answered", "প্রশ্নের উত্তর দেওয়া হয়নি")
                    .replace("Patient reports -", "রোগীর রিপোর্ট-")
                    .replace("Patient denies -", "রোগী অস্বীকার করে-")
                    .replace("Hours", "ঘন্টার").replace("Days", "দিনগুলি")
                    .replace("Weeks", "সপ্তাহ").replace("Months", "মাস")
                    .replace("Years", "বছর")
                    .replace("times per hour", "প্রতি ঘন্টা")
                    .replace("time per day", "দিনে বার")
                    .replace("times per week", "প্রতি সপ্তাহে বার")
                    .replace("times per month", "প্রতি মাসে বার")
                    .replace("times per year", "প্রতি বছর বার");
        } else if (locale.equalsIgnoreCase("ta")) {
            result = result.replace("Question not answered", "கேள்விக்கு பதில் அளிக்கப்படவில்லை")
                    .replace("Patient reports -", "நோயாளி கூறுகிறார்-")
                    .replace("Patient denies -", "நோயாளி மறுக்கிறார்-")
                    .replace("Hours", "மணி").replace("Days", "நாட்கள்")
                    .replace("Weeks", "வாரங்கள்").replace("Months", "மாதங்கள்")
                    .replace("Years", "ஆண்டுகள்")
                    .replace("times per hour", "ஒரு மணி நேரத்திற்கு முறை")
                    .replace("time per day", "ஒரு நாளைக்கு முறை")
                    .replace("times per week", "வாரத்திற்கு முறை")
                    .replace("times per month", "மாதம் முறை")
                    .replace("times per year", "வருடத்திற்கு முறை");
        }
        Log.v("VisitUtils", "OUT - "+result);

        return result;
    }

    public static String getTranslatedPatientDenies(String localeCode) {
        if (localeCode.equalsIgnoreCase("hi")) {
            return "पेशेंट ने मना कर दिया -";
        } else if (localeCode.equalsIgnoreCase("or")) {
            return "ରୋଗୀ ଅସ୍ୱୀକାର କରନ୍ତି -";
        } else {
            return "Patient denies -";
        }
    }

}
