/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.intelehealth.app.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.text.InputFilter;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import org.intelehealth.app.R;
import org.intelehealth.app.app.IntelehealthApplication;
import org.json.JSONArray;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public final class StringUtils {
    private static final String NULL_AS_STRING = "null";
    private static final String SPACE_CHAR = " ";

    public static boolean notNull(String string) {
        return null != string && !NULL_AS_STRING.equals(string.trim());
    }

    public static boolean isBlank(String string) {
        return null == string || SPACE_CHAR.equals(string);
    }

    public static boolean notEmpty(String string) {
        return string != null && !string.isEmpty();
    }

    public static String unescapeJavaString(String st) {

        StringBuilder sb = new StringBuilder(st.length());

        for (int i = 0; i < st.length(); i++) {
            char ch = st.charAt(i);
            if (ch == '\\') {
                char nextChar = (i == st.length() - 1) ? '\\' : st
                        .charAt(i + 1);
                // Octal escape?
                if (nextChar >= '0' && nextChar <= '7') {
                    String code = "" + nextChar;
                    i++;
                    if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                            && st.charAt(i + 1) <= '7') {
                        code += st.charAt(i + 1);
                        i++;
                        if ((i < st.length() - 1) && st.charAt(i + 1) >= '0'
                                && st.charAt(i + 1) <= '7') {
                            code += st.charAt(i + 1);
                            i++;
                        }
                    }
                    sb.append((char) Integer.parseInt(code, 8));
                    continue;
                }
                switch (nextChar) {
                    case '\\':
                        ch = '\\';
                        break;
                    case 'b':
                        ch = '\b';
                        break;
                    case 'f':
                        ch = '\f';
                        break;
                    case 'n':
                        ch = '\n';
                        break;
                    case 'r':
                        ch = '\r';
                        break;
                    case 't':
                        ch = '\t';
                        break;
                    case '\"':
                        ch = '\"';
                        break;
                    case '\'':
                        ch = '\'';
                        break;
                    // Hex Unicode: u????
                    case 'u':
                        if (i >= st.length() - 5) {
                            ch = 'u';
                            break;
                        }
                        int code = Integer.parseInt(
                                "" + st.charAt(i + 2) + st.charAt(i + 3)
                                        + st.charAt(i + 4) + st.charAt(i + 5), 16);
                        sb.append(Character.toChars(code));
                        i += 5;
                        continue;
                    default:
                        // Do nothing
                        break;
                }
                i++;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    public static String getValue(String value) {
        String val = "";
        if (value != null)
            val = value;
        return val;

    }

    public static String getValue1(String value) {
        String val = " ";
        if (value != null)
            val = value;
        return val;

    }

    public static String getProvided(Spinner spinner) {
        String val = "";
        if (spinner.getSelectedItemPosition() == 0)
            val = "Not provided";


        else if (spinner.getSelectedItem() == null) {
            val = "Not provided";

        } else {
            val = spinner.getSelectedItem().toString();
        }

        SessionManager sessionManager = new SessionManager(IntelehealthApplication.getAppContext());
        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            val = switch_hi_caste(val);
            val = switch_hi_economic(val);
            val = switch_hi_education(val);
            val = switch_hi_guardian_type(val);
            val = switch_hi_contact_type(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            val = switch_or_caste(val);
            val = switch_or_economic(val);
            val = switch_or_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
            val = switch_te_caste(val);
            val = switch_te_economic(val);
            val = switch_te_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            val = switch_mr_caste(val);
            val = switch_mr_economic(val);
            val = switch_mr_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            val = switch_as_caste(val);
            val = switch_as_economic(val);
            val = switch_as_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
            val = switch_ml_caste(val);
            val = switch_ml_economic(val);
            val = switch_ml_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            val = switch_kn_caste(val);
            val = switch_kn_economic(val);
            val = switch_kn_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ru")) {
            val = switch_ru_caste(val);
            val = switch_ru_economic(val);
            val = switch_ru_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            val = switch_gu_caste(val);
            val = switch_gu_economic(val);
            val = switch_gu_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            val = switch_bn_caste(val);
            val = switch_bn_economic(val);
            val = switch_bn_education(val);
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
            val = switch_ta_caste(val);
            val = switch_ta_economic(val);
            val = switch_ta_education(val);
        }

        return val;
    }

    public static String switch_ru_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "Безграмотный";
                break;
            case "Primary":
                val = "Начальный";
                break;
            case "Secondary":
                val = "Вторичный";
                break;
            case "Higher Secondary":
                val = "Высшее среднее";
                break;
            case "Graduation & Higher":
                val = "Выпускной и высшее";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_kn_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "ಅಶಿಕ್ಷಿತ";
                break;
            case "Primary":
                val = "ಒಂದನೆಯ";
                break;
            case "Secondary":
                val = "ದ್ವಿತೀಯ";
                break;
            case "Higher Secondary":
                val = "ಹೈಯರ್ ಸೆಕೆಂಡರಿ";
                break;
            case "Graduation & Higher":
                val = "ಪದವಿ ಮತ್ತು ಹೆಚ್ಚಿನ";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_kn_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "ಅಶಿಕ್ಷಿತ":
                val = "Illiterate";
                break;
            case "ಒಂದನೆಯ":
                val = "Primary";
                break;
            case "ದ್ವಿತೀಯ":
                val = "Secondary";
                break;
            case "ಹೈಯರ್ ಸೆಕೆಂಡರಿ":
                val = "Higher Secondary";
                break;
            case "ಪದವಿ ಮತ್ತು ಹೆಚ್ಚಿನ":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_ru_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Безграмотный":
                val = "Illiterate";
                break;
            case "Начальный":
                val = "Primary";
                break;
            case "Вторичный":
                val = "Secondary";
                break;
            case "Высшее среднее":
                val = "Higher Secondary";
                break;
            case "Выпускной и высшее":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "અશિક્ષિત";
                break;
            case "Primary":
                val = "પ્રાથમિક";
                break;
            case "Secondary":
                val = "સેકેંડરી";
                break;
            case "Higher Secondary":
                val = "હાયર સેકેંડરી";
                break;
            case "Graduation & Higher":
                val = "સ્નાતક અને વધુ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "અશિક્ષિત":
                val = "Illiterate";
                break;
            case "પ્રાથમિક":
                val = "Primary";
                break;
            case "સેકેંડરી":
                val = "Secondary";
                break;
            case "હાયર સેકેંડરી":
                val = "Higher Secondary";
                break;
            case "સ્નાતક અને વધુ":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_economic(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "ગરીબીની રેખાથી ઉપર":
                val = "APL";
                break;
            case "ગરીબીની રેખાથી નિચે":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "APL":
                val = "ગરીબીની રેખાથી ઉપર";
                break;
            case "BPL":
                val = "ગરીબીની રેખાથી નિચે";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_caste(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "જનરલ":
                val = "General";
                break;
            case "સામાજિક શૈક્ષણિક પછાત":
                val = "OBC";
                break;
            case "શેડ્યુલ કાસ્ટ":
                val = "SC";
                break;
            case "શેડ્યુલ ટ્રાઇબ":
                val = "ST";
                break;
            case "અન્ય":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_gu_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "General":
                val = "જનરલ";
                break;
            case "OBC":
                val = "સામાજિક શૈક્ષણિક પછાત";
                break;
            case "SC":
                val = "શેડ્યુલ કાસ્ટ";
                break;
            case "ST":
                val = "શેડ્યુલ ટ્રાઇબ";
                break;
            case "others":
                val = "અન્ય";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "अशिक्षित";
                break;
            case "Pre-Primary":
                val = "प्री-प्राइमरी";
                break;
            case "Primary":
                val = "प्रथम";
                break;
            case "Secondary":
                val = "माध्यमिक";
                break;
            case "Higher Secondary":
                val = "उच्च माध्यमिक";
                break;
            case "Graduate":
                val = "स्नातक";
                break;
            case "Masters & Higher":
                val = "परास्नातक एवं उच्चतर";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_hi_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "अशिक्षित":
                val = "Illiterate";
                break;
            case "प्री-प्राइमरी":
                val = "Pre-Primary";
                break;
            case "प्रथम":
                val = "Primary";
                break;
            case "माध्यमिक":
                val = "Secondary";
                break;
            case "उच्च माध्यमिक":
                val = "Higher Secondary";
                break;
            case "स्नातक":
                val = "Graduate";
                break;
            case "परास्नातक एवं उच्चतर":
                val = "Masters & Higher";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_or_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "ଅଶିକ୍ଷିତ";
                break;
            case "Primary":
                val = "ମୌଳିକ ଶିକ୍ଷା";
                break;
            case "Secondary":
                val = "ମାଧ୍ୟମିକ ଶିକ୍ଷା";
                break;
            case "Higher Secondary":
                val = "ଉଚ୍ଚ ମାଧ୍ୟମିକ ଶିକ୍ଷା";
                break;
            case "Graduation & Higher":
                val = "ସ୍ନାତକୋତର ଶିକ୍ଷା";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_or_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "ଅଶିକ୍ଷିତ":
                val = "Illiterate";
                break;
            case "ప్రాథమిక":
                val = "Primary";
                break;
            case "ద్వితీయ":
                val = "Secondary";
                break;
            case "హయ్యర్ సెకండరీ":
                val = "Higher Secondary";
                break;
            case "ସ୍ନାତକୋତର ଶିକ୍ଷା":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "নিৰক্ষৰ";
                break;
            case "Primary":
                val = "প্ৰাথমিক";
                break;
            case "Secondary":
                val = "গৌণ";
                break;
            case "Higher Secondary":
                val = "উচ্চতৰ মাধ্যমিক";
                break;
            case "Graduation & Higher":
                val = "স্নাতক আৰু উচ্চতৰ";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_as_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "নিৰক্ষৰ":
                val = "Illiterate";
                break;
            case "প্ৰাথমিক":
                val = "Primary";
                break;
            case "গৌণ":
                val = "Secondary";
                break;
            case "উচ্চতৰ মাধ্যমিক":
                val = "Higher Secondary";
                break;
            case "স্নাতক আৰু উচ্চতৰ":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ml_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "നിരക്ഷരൻ";
                break;
            case "Primary":
                val = "പ്രാഥമിക";
                break;
            case "Secondary":
                val = "സെക്കൻഡറി";
                break;
            case "Higher Secondary":
                val = "ഹയർ സെക്കൻഡറി";
                break;
            case "Graduation & Higher":
                val = "সബിരുദവും ഉന്നതവും";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_ml_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "നിരക്ഷരൻ":
                val = "Illiterate";
                break;
            case "പ്രാഥമിക":
                val = "Primary";
                break;
            case "സെക്കൻഡറി":
                val = "Secondary";
                break;
            case "ഹയർ സെക്കൻഡറി":
                val = "Higher Secondary";
                break;
            case "সബിരുദവും ഉന്നതവും":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_mr_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "निरक्षर";
                break;
            case "Primary":
                val = "प्राथमिक";
                break;
            case "Secondary":
                val = "दुय्यम";
                break;
            case "Higher Secondary":
                val = "उच्च माध्यमिक";
                break;
            case "Graduation & Higher":
                val = "पदवी आणि उच्च";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_mr_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "निरक्षर":
                val = "Illiterate";
                break;
            case "प्राथमिक":
                val = "Primary";
                break;
            case "दुय्यम":
                val = "Secondary";
                break;
            case "उच्च माध्यमिक":
                val = "Higher Secondary";
                break;
            case "पदवी आणि उच्च":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_te_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "నిరక్షరాస్యులు";
                break;
            case "Primary":
                val = "ମୌଳିକ ଶିକ୍ଷା";
                break;
            case "Secondary":
                val = "ద్వితీయ";
                break;
            case "Higher Secondary":
                val = "హయ్యర్ సెకండరీ";
                break;
            case "Graduation & Higher":
                val = "ସగ్రాడ్యుయేషన్ & ఉన్నత";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_te_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "నిరక్షరాస్యులు":
                val = "Illiterate";
                break;
            case "ప్రాథమిక":
                val = "Primary";
                break;
            case "ମାଧ୍ୟମିକ ଶିକ୍ଷା":
                val = "Secondary";
                break;
            case "ଉଚ୍ଚ ମାଧ୍ୟମିକ ଶିକ୍ଷା":
                val = "Higher Secondary";
                break;
            case "ସగ్రాడ్యుయేషన్ & ఉన్నత":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_bn_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "নিরক্ষর";
                break;
            case "Primary":
                val = "প্রাথমিক";
                break;
            case "Secondary":
                val = "মাধ্যমিক";
                break;
            case "Higher Secondary":
                val = "উচ্চ মাধ্যমিক";
                break;
            case "Graduation & Higher":
                val = "স্নাতক বা উচ্চতর";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_bn_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "নিরক্ষর":
                val = "Illiterate";
                break;
            case "প্রাথমিক":
                val = "Primary";
                break;
            case "মাধ্যমিক":
                val = "Secondary";
                break;
            case "উচ্চ মাধ্যমিক":
                val = "Higher Secondary";
                break;
            case "স্নাতক বা উচ্চতর":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ta_education_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Illiterate":
                val = "எழுதப்படிக்கத்தெரியாதவர்";
                break;
            case "Primary":
                val = "முதல்நிலை";
                break;
            case "Secondary":
                val = "இடைநிலை";
                break;
            case "Higher Secondary":
                val = "மேல்நிலை";
                break;
            case "Graduation & Higher":
                val = "பட்டம் மற்றும் உயர்";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_ta_education(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "எழுதப்படிக்கத்தெரியாதவர்":
                val = "Illiterate";
                break;
            case "முதல்நிலை":
                val = "Primary";
                break;
            case "இடைநிலை":
                val = "Secondary";
                break;
            case "மேல்நிலை":
                val = "Higher Secondary";
                break;
            case "பட்டம் மற்றும் உயர்":
                val = "Graduation & Higher";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_economic(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "गरीबी रेखा से ऊपर":
                val = "APL";
                break;
            case "गरीबी रेखा से नीचे":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "APL":
                val = "गरीबी रेखा से ऊपर";
                break;
            case "BPL":
                val = "गरीबी रेखा से नीचे";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ru_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "APL":
                val = "APL";
                break;
            case "BPL":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_economic(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "ଦାରିଦ୍ର୍ୟ ସୀମାରେଖା ଉପରେ":
                val = "APL";
                break;
            case "ଦାରିଦ୍ର୍ୟ ସୀମାରେଖା ତଳେ":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "APL":
                val = "ଦାରିଦ୍ର୍ୟ ସୀମାରେଖା ଉପରେ";
                break;
            case "BPL":
                val = "ଦାରିଦ୍ର୍ୟ ସୀମାରେଖା ତଳେ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_bn_economic(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "দারিদ্র্যসীমার উপরে":
                val = "APL";
                break;
            case "দারিদ্র্যসীমার নিচে":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_bn_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "APL":
                val = "দারিদ্র্যসীমার উপরে";
                break;
            case "BPL":
                val = "দারিদ্র্যসীমার নিচে";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_te_economic(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "ఎపిఎల్":
                val = "APL";
                break;
            case "బిపిఎల్":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_te_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "APL":
                val = "ఎపిఎల్";
                break;
            case "BPL":
                val = "బిపిఎల్";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_mr_economic(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "दारिद्र्य रेषेच्या वर":
                val = "APL";
                break;
            case "दारिद्र्य रेषेखाली":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_mr_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "APL":
                val = "दारिद्र्य रेषेच्या वर";
                break;
            case "BPL":
                val = "दारिद्र्य रेषेखाली";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_economic(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "এপিএল":
                val = "APL";
                break;
            case "বিপিএল":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "APL":
                val = "এপিএল";
                break;
            case "BPL":
                val = "বিপিএল";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ml_economic(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "എ.പി.എൽ":
                val = "APL";
                break;
            case "ബിപിഎൽ":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ml_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "APL":
                val = "എ.പി.എൽ";
                break;
            case "BPL":
                val = "ബിപിഎൽ";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String switch_ru_economic(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "APL":
                val = "APL";
                break;
            case "BPL":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_kn_economic(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "ಬಡತನ ರೇಖೆಯ ಮೇಲೆ":
                val = "APL";
                break;
            case "ಬಡತನ ರೇಖೆಯ ಕೆಳಗೆ":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_kn_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "APL":
                val = "ಬಡತನ ರೇಖೆಯ ಮೇಲೆ";
                break;
            case "BPL":
                val = "ಬಡತನ ರೇಖೆಯ ಕೆಳಗೆ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ta_economic(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "வறுமைக் கோட்டிற்கு மேல்":
                val = "APL";
                break;
            case "வறுமைக் கோட்டிற்குக் கீழ்":
                val = "BPL";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ta_economic_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "APL":
                val = "வறுமைக் கோட்டிற்கு மேல்";
                break;
            case "BPL":
                val = "வறுமைக் கோட்டிற்குக் கீழ்";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_caste(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "सामान्य":
                val = "General";
                break;
            case "अन्य पिछड़ा वर्ग":
                val = "OBC";
                break;
            case "अनुसूचित जाति":
                val = "SC";
                break;
            case "अनुसूचित जनजाति":
                val = "ST";
                break;
            case "अन्य":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_caste(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "ସାଧାରଣ":
                val = "General";
                break;
            case "ଅନ୍ୟାନ୍ୟ ପଛୁଆ ବର୍ଗ":
                val = "OBC";
                break;
            case "ଅନୁସୂଚିତ ଜାତି":
                val = "SC";
                break;
            case "ଅନୁସୂଚିତ ଜନଜାତି":
                val = "ST";
                break;
            case "ଅନ୍ୟାନ୍ୟ":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ta_caste(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "பொது":
                val = "General";
                break;
            case "ஓபிசி":
                val = "OBC";
                break;
            case "எஸ்சி":
                val = "SC";
                break;
            case "எஸ்டி":
                val = "ST";
                break;
            case "மற்றவை":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_te_caste(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "జనరల్":
                val = "General";
                break;
            case "ఓబిసి":
                val = "OBC";
                break;
            case "ఎస్సీ":
                val = "SC";
                break;
            case "ఎస్టీ":
                val = "ST";
                break;
            case "ఇతరులు":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_mr_caste(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "सामान्य":
                val = "General";
                break;
            case "इतर मागासवर्गीय":
                val = "OBC";
                break;
            case "अनुसूचित जाती":
                val = "SC";
                break;
            case "अनुसूचित जमाती":
                val = "ST";
                break;
            case "इतर":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_caste(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "সাধাৰণ":
                val = "General";
                break;
            case "অবিচি":
                val = "OBC";
                break;
            case "উচ্চতম ন্যায়ালয়":
                val = "SC";
                break;
            case "এচটি":
                val = "ST";
                break;
            case "আন":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_mr_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "General":
                val = "सामान्य";
                break;
            case "OBC":
                val = "इतर मागासवर्गीय";
                break;
            case "SC":
                val = "अनुसूचित जाती";
                break;
            case "ST":
                val = "अनुसूचित जमाती";
                break;
            case "others":
                val = "इतर";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ru_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "General":
                val = "Общий";
                break;
            case "OBC":
                val = "OBC";
                break;
            case "SC":
                val = "SC";
                break;
            case "ST":
                val = "ST";
                break;
            case "others":
                val = "другие";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ru_caste(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "Общий":
                val = "General";
                break;
            case "OBC":
                val = "OBC";
                break;
            case "SC":
                val = "SC";
                break;
            case "ST":
                val = "ST";
                break;
            case "другие":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_kn_caste(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "ಸಾಮಾನ್ಯ":
                val = "General";
                break;
            case "ಓ.ಬಿ.ಸಿ.":
                val = "OBC";
                break;
            case "ಎಸ್ಸಿ":
                val = "SC";
                break;
            case "ಪರಿಶಿಷ್ಟ ಪಂಗಡ":
                val = "ST";
                break;
            case "ಇತರ":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_kn_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "General":
                val = "ಸಾಮಾನ್ಯ";
                break;
            case "OBC":
                val = "ಓ.ಬಿ.ಸಿ.";
                break;
            case "SC":
                val = "ಎಸ್ಸಿ";
                break;
            case "ST":
                val = "ಪರಿಶಿಷ್ಟ ಪಂಗಡ";
                break;
            case "others":
                val = "ಇತರ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ml_caste(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "ജനറൽ":
                val = "General";
                break;
            case "ഒ.ബി.സി.":
                val = "OBC";
                break;
            case "എസ്.സി.":
                val = "SC";
                break;
            case "എസ്.ടി":
                val = "ST";
                break;
            case "മറ്റുള്ളവർ":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ml_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "General":
                val = "ജനറൽ";
                break;
            case "OBC":
                val = "ഒ.ബി.സി.";
                break;
            case "SC":
                val = "എസ്.സി.";
                break;
            case "ST":
                val = "എസ്.ടി";
                break;
            case "others":
                val = "മറ്റുള്ളവർ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_as_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "General":
                val = "সাধাৰণ";
                break;
            case "OBC":
                val = "অবিচি";
                break;
            case "SC":
                val = "উচ্চতম ন্যায়ালয়";
                break;
            case "ST":
                val = "এচটি";
                break;
            case "others":
                val = "আন";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_bn_caste(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "সাধারণ":
                val = "General";
                break;
            case "ওবিসি":
                val = "OBC";
                break;
            case "এসসি":
                val = "SC";
                break;
            case "এসটি":
                val = "ST";
                break;
            case "অন্যান্য":
                val = "others";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_hi_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "General":
                val = "सामान्य";
                break;
            case "OBC":
                val = "अन्य पिछड़ा वर्ग";
                break;
            case "SC":
                val = "अनुसूचित जाति";
                break;
            case "ST":
                val = "अनुसूचित जनजाति";
                break;
            case "others":
                val = "अन्य";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_or_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "General":
                val = "ସାଧାରଣ";
                break;
            case "OBC":
                val = "ଅନ୍ୟାନ୍ୟ ପଛୁଆ ବର୍ଗ";
                break;
            case "SC":
                val = "ଅନୁସୂଚିତ ଜାତି";
                break;
            case "ST":
                val = "ଅନୁସୂଚିତ ଜନଜାତି";
                break;
            case "others":
                val = "ଅନ୍ୟାନ୍ୟ";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_te_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "General":
                val = "జనరల్";
                break;
            case "OBC":
                val = "ఓబిసి";
                break;
            case "SC":
                val = "ఎస్సీఎస్సీ";
                break;
            case "ST":
                val = "ఎస్టీ";
                break;
            case "others":
                val = "ఇతరులు";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_bn_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "General":
                val = "সাধারণ";
                break;
            case "OBC":
                val = "ওবিসি";
                break;
            case "SC":
                val = "এসসি";
                break;
            case "ST":
                val = "এসটি";
                break;
            case "others":
                val = "অন্যান্য";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_ta_caste_edit(String val) {
        if (val == null || val.isEmpty()) return "";

        switch (val) {
            case "General":
                val = "பொது";
                break;
            case "OBC":
                val = "ஓபிசி";
                break;
            case "SC":
                val = "எஸ்சி";
                break;
            case "ST":
                val = "எஸ்டி";
                break;
            case "others":
                val = "மற்றவை";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String getFileNameWithoutExtension(File file) {

        String fileName = "";

        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                fileName = name.replaceFirst("[.][^.]+$", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
        }

        return fileName;

    }

    public static String getFileNameWithoutExtensionString(String filename) {
        String fileName = "";

        try {
            if (filename.indexOf(".") > 0)
                fileName = filename.substring(0, filename.lastIndexOf("."));
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
        }

        return fileName;

    }


    public static String switch_hi_Country_edit(String val) {
        switch (val) {
            case "General":
                val = "इंडिया";
                break;
            case "OBC":
                val = "संयुक्त राज्य अमेरिका";
                break;
            case "SC":
                val = "फिलीपींस";
                break;
            default:
                return val;
        }
        return val;
    }

    public static String switch_te_Country_edit(String val) {
        switch (val) {
            case "General":
                val = "सामान्य";
                break;
            case "OBC":
                val = "अन्य पिछड़ा वर्ग";
                break;
            case "SC":
                val = "अनुसूचित जाति";
                break;
            case "ST":
                val = "अनुसूचित जनजाति";
                break;
            case "others":
                val = "अन्य";
                break;
            default:
                return val;
        }
        return val;
    }


    public static String convertUsingStringBuilder(List<String> names) {
        StringBuilder namesStr = new StringBuilder();
        for (String name : names) {
            namesStr = namesStr.length() > 0 ? namesStr.append("','").append(name) : namesStr.append(name);
        }
        return namesStr.toString();
    }

    public static String mobileNumberEmpty(String value) {
        String val = "N/A";
        if (value != null && !value.isEmpty())
            val = value;
        return val;
    }

    public static String en__hi_dob(String dob) { //English dob is replaced to Hindi text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "जनवरी")
                .replace("February", "फ़रवरी")
                .replace("March", "मार्च")
                .replace("April", "अप्रैल")
                .replace("May", "मई")
                .replace("June", "जून")
                .replace("July", "जुलाई")
                .replace("August", "अगस्त")
                .replace("September", "सितंबर")
                .replace("October", "अक्टूबर")
                .replace("November", "नवंबर")
                .replace("December", "दिसंबर");

        return mdob_text;
    }


    public static String getMessageTranslated(String message, String locale) { //English dob is replaced to Hindi text.
        if (message == null || message.isEmpty()) return "";
        if (locale.equalsIgnoreCase("hi")) {
            message = message
                    .replace("Otp sent successfully!", "ओटीपी सफलतापूर्वक भेजा गया!")
                    .replace("No user exists with this phone number/email/username.",
                            "इस फ़ोन नंबर/ईमेल के साथ कोई उपयोगकर्ता मौजूद नहीं है।")
                    .replace("No phoneNumber/email updated for this username.",
                            "इस उपयोगकर्ता नाम के लिए कोई फ़ोन नंबर/ईमेल अपडेट नहीं किया गया है।")
                    .replace("No user exists with this username.",
                            "इस उपयोगकर्ता नाम के साथ कोई उपयोगकर्ता मौजूद नहीं है।")
                    .replace("Otp verified successfully!", "ओटीपी सफलतापूर्वक सत्यापित!")
                    .replace("Otp expired!", "ओटीपी एक्स्पायर्ड हो गया!")
                    .replace("Otp incorrect!", "ओटीपी गलत है।")
                    .replace("Password reset successful.", "पासवर्ड रीसेट सफल रहा।")
                    .replace("No user exists!", "कोई उपयोगकर्ता मौजूद नहीं है!");
        }
        return message;
    }


    public static String en_hi_dob_updated(String dob) { //English dob is replaced to Hindi text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("Jan", "जनवरी")
                .replace("Feb", "फ़रवरी")
                .replace("March", "मार्च")
                .replace("April", "अप्रैल")
                .replace("May", "मई")
                .replace("June", "जून")
                .replace("July", "जुलाई")
                .replace("Aug", "अगस्त")
                .replace("Sept", "सितंबर")
                .replace("Oct", "अक्टूबर")
                .replace("Nov", "नवंबर")
                .replace("Dec", "दिसंबर");

        return mdob_text;
    }

    public static String en_hi_dob_three(String dob) { //English dob is replaced to Hindi text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("Jan", "जनवरी")
                .replace("Feb", "फ़रवरी")
                .replace("Mar", "मार्च")
                .replace("Apr", "अप्रैल")
                .replace("May", "मई")
                .replace("Jun", "जून")
                .replace("Jul", "जुलाई")
                .replace("Aug", "अगस्त")
                .replace("Sep", "सितंबर")
                .replace("Oct", "अक्टूबर")
                .replace("Nov", "नवंबर")
                .replace("Dec", "दिसंबर");

        return mdob_text;
    }


    public static String en__gu_dob(String dob) { //English dob is replaced to Hindi text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "જાન્યુઆરી")
                .replace("February", "ફેબ્રુઆરી")
                .replace("March", "કુચ")
                .replace("April", "એપ્રિલ")
                .replace("May", "મે")
                .replace("June", "જૂન")
                .replace("July", "જુલાઈ")
                .replace("August", ".ગસ્ટ")
                .replace("September", "સપ્ટેમ્બર")
                .replace("October", "ઓક્ટોબર")
                .replace("November", "નવેમ્બર")
                .replace("December", "ડિસેમ્બર");

        return mdob_text;
    }

    public static String en__or_dob(String dob) { //English dob is replaced to Odiya text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "ଜାନୁଆରୀ")
                .replace("February", "ଫେବୃଆରୀ")
                .replace("March", "ମାର୍ଚ୍ଚ")
                .replace("April", "ଏପ୍ରିଲ୍")
                .replace("May", "ମେ")
                .replace("June", "ଜୁନ୍")
                .replace("July", "ଜୁଲାଇ")
                .replace("August", "ଅଗଷ୍ଟ")
                .replace("September", "ସେପ୍ଟେମ୍ବର")
                .replace("October", "ଅକ୍ଟୋବର")
                .replace("November", "ନଭେମ୍ବର")
                .replace("December", "ଡିସେମ୍ବର");

        return mdob_text;
    }

    /**
     * ritika
     * String Value According the Languages
     *
     * @param dob= dob to be change into telugu
     * @return String telugu dob According the Languages
     */

    public static String en__te_dob(String dob) { //English dob is replaced to Tamil text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "జనవరి")
                .replace("February", "ఫిబ్రవరి")
                .replace("March", "మార్చి")
                .replace("April", "ఏప్రిల్")
                .replace("May", "మే")
                .replace("June", "జూన్")
                .replace("July", "జూలై")
                .replace("August", "ఆగస్టు")
                .replace("September", "సెప్టెంబర్")
                .replace("October", "అక్టోబర్")
                .replace("November", "నవంబర్")
                .replace("December", "డిసెంబర్");

        return mdob_text;
    }

    public static String en__mr_dob(String dob) { //English dob is replaced to marathi text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "जानेवारी")
                .replace("February", "फेब्रुवारी")
                .replace("March", "मार्च")
                .replace("April", "एप्रिल")
                .replace("May", "मे")
                .replace("June", "जून")
                .replace("July", "जुलै")
                .replace("August", "ऑगस्ट")
                .replace("September", "सप्टेंबर")
                .replace("October", "ऑक्टोबर")
                .replace("November", "नोव्हेंबर")
                .replace("December", "डिसेंबर");

        return mdob_text;
    }

    public static String en__as_dob(String dob) { //English dob is replaced to marathi text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "জানুৱাৰী")
                .replace("February", "ফেব্ৰুৱাৰী")
                .replace("March", "মাৰ্চ")
                .replace("April", "এপ্ৰিল")
                .replace("May", "মে")
                .replace("June", "জুন")
                .replace("July", "জুলাই")
                .replace("August", "আগষ্ট")
                .replace("September", "ছেপ্টেম্বৰ")
                .replace("October", "অক্টোবৰ")
                .replace("November", "নৱেম্বৰ")
                .replace("December", "ডিচেম্বৰ");

        return mdob_text;
    }

    public static String en__ml_dob(String dob) { //English dob is replaced to marathi text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "ജനുവരി")
                .replace("February", "ഫെബ്രുവരി")
                .replace("March", "മാർച്ച്")
                .replace("April", "ഏപ്രിൽ")
                .replace("May", "മെയ്")
                .replace("June", "ജൂൺ")
                .replace("July", "ജൂലൈ")
                .replace("August", "ആഗസ്റ്റ്")
                .replace("September", "സെപ്റ്റംബർ")
                .replace("October", "ഒക്ടോബർ")
                .replace("November", "നവംബർ")
                .replace("December", "ഡിസംബർ");

        return mdob_text;
    }

    public static String en__kn_dob(String dob) { //English dob is replaced to marathi text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "ಜನವರಿ")
                .replace("February", "ಫೆಬ್ರುವರಿ")
                .replace("March", "ಮಾರ್ಚ್")
                .replace("April", "ಏಪ್ರಿಲ್")
                .replace("May", "ಮೇ")
                .replace("June", "ಜೂನ")
                .replace("July", "ಜುಲೈ")
                .replace("August", "ಆಗಸ್ಟ್")
                .replace("September", "ಸೆಪ್ಟೆಂಬರ್")
                .replace("October", "ಅಕ್ಟೋಬರ್")
                .replace("November", "ನವೆಂಬರ್")
                .replace("December", "ಡಿಸೆಂಬರ್");

        return mdob_text;
    }

    public static String en__ru_dob(String dob) { //English dob is replaced to Odiya text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "Январь")
                .replace("February", "Февраль")
                .replace("March", "Март")
                .replace("April", "апреля")
                .replace("May", "Май")
                .replace("June", "июнь")
                .replace("July", "июль")
                .replace("August", "август")
                .replace("September", "сентябрь")
                .replace("October", "Октябрь")
                .replace("November", "Ноябрь")
                .replace("December", "Декабрь");

        return mdob_text;
    }

    public static String en__bn_dob(String dob) { //English dob is replaced to Bengali text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "জানুয়ারী")
                .replace("February", "ফেব্রুয়ারী")
                .replace("March", "মার্চ")
                .replace("April", "এপ্রিল")
                .replace("May", "মে")
                .replace("June", "জুন")
                .replace("July", "জুলাই")
                .replace("August", "আগস্ট")
                .replace("September", "সেপ্টেম্বর")
                .replace("October", "অক্টোবর")
                .replace("November", "নভেম্বর")
                .replace("December", "ডিসেম্বর");

        return mdob_text;
    }

    public static String en__ta_dob(String dob) { //English dob is replaced to Tamil text.
        //added this logic to handle crash when dob is null
        if (dob == null || dob.isEmpty()) return "";
        String mdob_text = dob
                .replace("January", "ஜனவரி")
                .replace("February", "பிப்ரவரி")
                .replace("March", "மார்ச்")
                .replace("April", "ஏப்ரல்")
                .replace("May", "மே")
                .replace("June", "ஜூன்")
                .replace("July", "ஜூலை")
                .replace("August", "ஆகஸ்ட்")
                .replace("September", "செப்டம்பர்")
                .replace("October", "அக்டோபர்")
                .replace("November", "நவம்பர்")
                .replace("December", "டிசம்பர்");

        return mdob_text;
    }

    public static String hi_or_bn_en_month(int month_index) {
        String dob_string = "";

        switch (month_index) {
            case 0:
                dob_string = "January";
                break;
            case 1:
                dob_string = "February";
                break;
            case 2:
                dob_string = "March";
                break;
            case 3:
                dob_string = "April";
                break;
            case 4:
                dob_string = "May";
                break;
            case 5:
                dob_string = "June";
                break;
            case 6:
                dob_string = "July";
                break;
            case 7:
                dob_string = "August";
                break;
            case 8:
                dob_string = "September";
                break;
            case 9:
                dob_string = "October";
                break;
            case 10:
                dob_string = "November";
                break;
            case 11:
                dob_string = "December";
                break;

            default:
                return dob_string;
        }

        return dob_string;
    }

    public static String getFullMonthName(String dobString) {
        //added this logic to handle crash when dob is null
        if (dobString == null || dobString.isEmpty()) return "";
        return dobString
                .replace("Jan", "January")
                .replace("Feb", "February")
                .replace("Mar", "March")
                .replace("Apr", "April")
                .replace("Jun", "June")
                .replace("Jul", "July")
                .replace("Aug", "August")
                .replace("Sept", "September")
                .replace("Oct", "October")
                .replace("Nov", "November")
                .replace("Dec", "December");

    }

    public static String hi_or_bn_en_noEdit(String dobString, String locale) {

        if (locale.equalsIgnoreCase("hi")) {
            String dob = dobString
                    //Hindi
                    .replace("जनवरी", "January")
                    .replace("फ़रवरी", "February")
                    .replace("मार्च", "March")
                    .replace("अप्रैल", "April")
                    .replace("मई", "May")
                    .replace("जून", "June")
                    .replace("जुलाई", "July")
                    .replace("अगस्त", "August")
                    .replace("सितंबर", "September")
                    .replace("अक्टूबर", "October")
                    .replace("नवंबर", "November")
                    .replace("दिसंबर", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("or")) {
            //Odiya
            String dob = dobString
                    .replace("ଜାନୁଆରୀ", "January")
                    .replace("ଫେବୃଆରୀ", "February")
                    .replace("ମାର୍ଚ୍ଚ", "March")
                    .replace("ଏପ୍ରିଲ୍", "April")
                    .replace("ମେ", "May")
                    .replace("ଜୁନ୍", "June")
                    .replace("ଜୁଲାଇ", "July")
                    .replace("ଅଗଷ୍ଟ", "August")
                    .replace("ସେପ୍ଟେମ୍ବର", "September")
                    .replace("ଅକ୍ଟୋବର", "October")
                    .replace("ନଭେମ୍ବର", "November")
                    .replace("ଡିସେମ୍ବର", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("gu")) {
            String dob = dobString
                    //Gujarati
                    .replace("જાન્યુઆરી", "January")
                    .replace("ફેબ્રુઆરી", "February")
                    .replace("કુચ", "March")
                    .replace("એપ્રિલ", "April")
                    .replace("મે", "May")
                    .replace("જૂન", "June")
                    .replace("જુલાઈ", "July")
                    .replace(".ગસ્ટ", "August")
                    .replace("સપ્ટેમ્બર", "September")
                    .replace("ઓક્ટોબર", "October")
                    .replace("નવેમ્બર", "November")
                    .replace("ડિસેમ્બર", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("te")) {
            //Telugu
            String dob = dobString
                    .replace("జనవరి", "January")
                    .replace("ఫిబ్రవరి", "February")
                    .replace("మార్చి", "March")
                    .replace("ఏప్రిల్", "April")
                    .replace("మే", "May")
                    .replace("జూన్", "June")
                    .replace("జూలై", "July")
                    .replace("ఆగస్టు", "August")
                    .replace("సెప్టెంబర్", "September")
                    .replace("అక్టోబర్", "October")
                    .replace("నవంబర్", "November")
                    .replace("డిసెంబర్", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("mr")) {
            //Marathi
            String dob = dobString
                    .replace("जानेवारी", "January")
                    .replace("फेब्रुवारी", "February")
                    .replace("मार्च", "March")
                    .replace("एप्रिल", "April")
                    .replace("मे", "May")
                    .replace("जून", "June")
                    .replace("जुलै", "July")
                    .replace("ऑगस्ट", "August")
                    .replace("सप्टेंबर", "September")
                    .replace("ऑक्टोबर", "October")
                    .replace("नोव्हेंबर", "November")
                    .replace("डिसेंबर", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("as")) {
            //Marathi
            String dob = dobString
                    .replace("জানুৱাৰী", "January")
                    .replace("ফেব্ৰুৱাৰী", "February")
                    .replace("মাৰ্চ", "March")
                    .replace("এপ্ৰিল", "April")
                    .replace("মে", "May")
                    .replace("জুন", "June")
                    .replace("জুলাই", "July")
                    .replace("আগষ্ট", "August")
                    .replace("ছেপ্টেম্বৰ", "September")
                    .replace("অক্টোবৰ", "October")
                    .replace("নৱেম্বৰ", "November")
                    .replace("ডিচেম্বৰ", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("ml")) {
            //Marathi
            String dob = dobString
                    .replace("ജനുവരി", "January")
                    .replace("ഫെബ്രുവരി", "February")
                    .replace("മാർച്ച്", "March")
                    .replace("ഏപ്രിൽ", "April")
                    .replace("മെയ്", "May")
                    .replace("ജൂൺ", "June")
                    .replace("ജൂലൈ", "July")
                    .replace("ആഗസ്റ്റ്", "August")
                    .replace("സെപ്റ്റംബർ", "September")
                    .replace("ഒക്ടോബർ", "October")
                    .replace("നവംബർ", "November")
                    .replace("ഡിസംബർ", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("kn")) {
            //kannad
            String dob = dobString
                    .replace("ಜನವರಿ", "January")
                    .replace("ಫೆಬ್ರುವರಿ", "February")
                    .replace("ಮಾರ್ಚ್", "March")
                    .replace("ಏಪ್ರಿಲ್", "April")
                    .replace("ಮೇ", "May")
                    .replace("ಜೂನ", "June")
                    .replace("ಜುಲೈ", "July")
                    .replace("ಆಗಸ್ಟ್", "August")
                    .replace("ಸೆಪ್ಟೆಂಬರ್", "September")
                    .replace("ಅಕ್ಟೋಬರ್", "October")
                    .replace("ನವೆಂಬರ್", "November")
                    .replace("ಡಿಸೆಂಬರ್", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("ta")) {
            //Bengali
            String dob = dobString
                    .replace("ஜனவரி", "January")
                    .replace("பிப்ரவரி", "February")
                    .replace("மார்ச்", "March")
                    .replace("ஏப்ரல்", "April")
                    .replace("மே", "May")
                    .replace("ஜூன்", "June")
                    .replace("ஜூலை", "July")
                    .replace("ஆகஸ்ட்", "August")
                    .replace("செப்டம்பர்", "September")
                    .replace("அக்டோபர்", "October")
                    .replace("நவம்பர்", "November")
                    .replace("டிசம்பர்", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("bn")) {
            //Bengali
            String dob = dobString
                    .replace("জানুয়ারী", "January")
                    .replace("ফেব্রুয়ারী", "February")
                    .replace("মার্চ", "March")
                    .replace("এপ্রিল", "April")
                    .replace("মে", "May")
                    .replace("জুন", "June")
                    .replace("জুলাই", "July")
                    .replace("আগস্ট", "August")
                    .replace("সেপ্টেম্বর", "September")
                    .replace("অক্টোবর", "October")
                    .replace("নভেম্বর", "November")
                    .replace("ডিসেম্বর", "December");
            return dob;
        } else if (locale.equalsIgnoreCase("ru")) {
            String dob = dobString
                    //Russian
                    .replace("Январь", "January")
                    .replace("Февраль", "February")
                    .replace("Март", "March")
                    .replace("апреля", "April")
                    .replace("Май", "May")
                    .replace("июнь", "June")
                    .replace("июль", "July")
                    .replace("август", "August")
                    .replace("сентябрь", "September")
                    .replace("Октябрь", "October")
                    .replace("Ноябрь", "November")
                    .replace("Декабрь", "December");
            return dob;
        } else {
            return dobString;
        }

    }

    public static String getLocaleGender(Context context, String gender) {
        switch (gender) {
            case "M":
                return context.getString(R.string.gender_male);
            case "F":
                return context.getString(R.string.gender_female);
            default:
                return gender;
        }
    }

    /**
     * Ritika======
     * get only selective Language from Db and Show accorfingly
     *
     * @param val    db Values in en locals only
     * @param locale selective app language locale.
     * @return return String value according the Locals to show on CountrySpinner.
     */
    public static String mSwitch_hi_en_te_Country_edit(String val, String locale) {
        if (locale.equalsIgnoreCase("hi")) {

            switch (val) {
                case "India":
                    val = "इंडिया";
                    break;
                case "United States":
                    val = "संयुक्त राज्य अमेरिका";
                    break;
                case "Philippines":
                    val = "फिलीपींस";
                    break;
                default:
                    return val;
            }

        }
        if (locale.equalsIgnoreCase("te")) {

            switch (val) {
                case "India":
                    val = "భారతదేశం";
                    break;
                case "United States":
                    val = "సంయుక్త రాష్ట్రాలు";
                    break;
                case "Philippines":
                    val = "ఫిలిప్పీన్స్";
                    break;

                default:
                    return val;
            }

        }
        if (locale.equalsIgnoreCase("or")) {

            switch (val) {
                case "India":
                    val = "इंडिया";
                    break;
                case "United States":
                    val = "संयुक्त राज्य अमेरिका";
                    break;
                case "Philippines":
                    val = "फिलीपींस";
                    break;
                default:
                    return val;
            }

        } else {
            return val;
        }
        return val;
    }

    public static String translateLocation(String val, String locale) {
        if (locale.equalsIgnoreCase("hi")) {
            if (val == null || val.isEmpty()) return "";
            switch (val) {
                case "Telemedicine Clinic 1":
                    val = "टेलीमेडिसिन क्लीनिक 1";
                    break;
                case "Telemedicine Clinic 2":
                    val = "टेलीमेडिसिन क्लीनिक 2";
                    break;
                case "Telemedicine Clinic 3":
                    val = "टेलीमेडिसिन क्लीनिक 3";
                    break;
                case "Remote":
                    val = "रिमोट";
                    break;
                default:
                    return val;
            }

        }
        return val;
    }

    /**
     * Ritika======
     * put only english Language into Db
     *
     * @param val    db Values in en locals only
     * @param locale selective app language locale.
     * @return return String value only english .
     */
    public static String mSwitch_hi_en_te_Country(String val, String locale) {
        if (locale.equalsIgnoreCase("hi")) {

            switch (val) {
                case "इंडिया":
                    val = "India";
                    break;
                case "संयुक्त राज्य अमेरिका":
                    val = "United States";
                    break;
                case "फिलीपींस":
                    val = "Philippines";
                    break;
                default:
                    return val;
            }

        }
        if (locale.equalsIgnoreCase("te")) {

            switch (val) {
                case "భారతదేశం":
                    val = "India";
                    break;
                case "సంయుక్త రాష్ట్రాలు":
                    val = "United States";
                    break;
                case "ఫిలిప్పీన్స్":
                    val = "Philippines";
                    break;

                default:
                    return val;
            }

        }

        return val;
    }

    public static String switch_hi_en_country(String val, String locale) {
        if (locale.equalsIgnoreCase("hi")) {
            if (val == null || val.isEmpty()) return "";
            switch (val) {
                case "India":
                    val = "इंडिया";
                    break;
                case "United States":
                    val = "संयुक्त राज्य अमेरिका";
                    break;
                case "Philippines":
                    val = "फिलीपींस";
                    break;
                default:
                    return val;
            }

        }
        return val;
    }

    /**
     * Ritika======
     * get only selective Language from Db and Show accordingly
     *
     * @param val    db Values in en locals only
     * @param locale selective app language locale.
     * @return return String value according the Locals to show on CountrySpinner.
     */

    public static String mSwitch_hi_en_te_State_edit(String val, String locale) {
        if (locale.equalsIgnoreCase("hi")) {
            switch (val) {
                case "Andhra Pradesh":
                    val = "आंध्र प्रदेश";
                    break;
                case "Arunachal Pradesh":
                    val = "अरुणाचल प्रदेश";
                    break;
                case "Assam":
                    val = "असम";
                    break;
                case "Bihar":
                    val = "बिहार";
                    break;
                case "Chandigarh (UT)":
                    val = "चंडीगढ़";
                    break;
                case "Chhattisgarh":
                    val = "छत्तीसगढ";
                    break;
                case "Dadra and Nagar Haveli (UT)":
                    val = "दादरा और नगर हवेली";
                    break;
                case "Daman and Diu (UT)":
                    val = "दमन और दीव";
                    break;
                case "Delhi (NCT)":
                    val = "दिल्ली";
                    break;
                case "Goa":
                    val = "गोवा";
                    break;
                case "Gujarat":
                    val = "गुजरात";
                    break;
                case "Haryana":
                    val = "हरियाणा";
                    break;
                case "Himachal Pradesh":
                    val = "हिमाचल प्रदेश";
                    break;
                case "Jammu and Kashmir":
                    val = "जम्मू और कश्मीर";
                    break;
                case "Jharkhand":
                    val = "झारखंड";
                    break;
                case "Karnataka":
                    val = "कर्नाटक";
                    break;
                case "Kerala":
                    val = "केरल";
                    break;
                case "Lakshadweep (UT)":
                    val = "लक्षद्वीप";
                    break;
                case "Madhya Pradesh":
                    val = "मध्य प्रदेश";
                    break;
                case "Maharashtra":
                    val = "महाराष्ट्र";
                    break;
                case "Manipur":
                    val = "मणिपुर";
                    break;
                case "Meghalaya":
                    val = "मेघालय";
                    break;
                case "Mizoram":
                    val = "मिजोरम";
                    break;
                case "Nagaland":
                    val = "नगालैंड";
                    break;
                case "Odisha":
                    val = "ओडिशा";
                    break;
                case "Puducherry (UT)":
                    val = "पुडुचेरी (यूटी)";
                    break;
                case "Punjab":
                    val = "पंजाब";
                    break;
                case "Rajasthan":
                    val = "राजस्थान";
                    break;
                case "Sikkim":
                    val = "सिक्किम";
                    break;
                case "Tamil Nadu":
                    val = "तमिलनाडु";
                    break;
                case "Telangana":
                    val = "तेलंगाना";
                    break;
                case "Tripura":
                    val = "त्रिपुरा";
                    break;
                case "Uttar Pradesh":
                    val = "उत्तर प्रदेश";
                    break;
                case "Uttarakhand":
                    val = "उत्तराखंड";
                    break;
                case "West Bengal":
                    val = "पश्चिम बंगाल";
                    break;

                //us states

                case "Alabama":
                    val = "अलाबामा";
                    break;
                case "Alaska":
                    val = "अलास्का";
                    break;
                case "Arizona":
                    val = "एरिज़ोना";
                    break;
                case "Arkansas":
                    val = "अर्कांसासो";
                    break;
                case "California":
                    val = "कैलिफोर्निया";
                    break;
                case "Colorado":
                    val = "कोलोराडो";
                    break;
                case "Connecticut":
                    val = "कनेक्टिकट";
                    break;
                case "Delaware":
                    val = "डेलावेयर";
                    break;
                case "Florida":
                    val = "फ्लोरिडा";
                    break;
                case "Georgia":
                    val = "जॉर्जिया";
                    break;
                case "Hawaii":
                    val = "हवाई";
                    break;
                case "Idaho":
                    val = "इडाहो";
                    break;
                case "Illinois":
                    val = "इलिनोइस";
                    break;
                case "Indiana":
                    val = "इंडियाना";
                    break;
                case "Iowa":
                    val = "आयोवा";
                    break;
                case "Kansas":
                    val = "कान्सास";
                    break;
                case "Kentucky":
                    val = "केंटकी";
                    break;
                case "Louisiana":
                    val = "लुइसियाना";
                    break;
                case "Maine":
                    val = "मैंने";
                    break;
                case "Maryland":
                    val = "मैरीलैंड";
                    break;
                case "Massachusetts":
                    val = "मैसाचुसेट्स";
                    break;
                case "Michigan":
                    val = "मिशिगन";
                    break;
                case "Minnesota":
                    val = "मिनेसोटा";
                    break;
                case "Mississippi":
                    val = "मिसीसिपी";
                    break;
                case "Missouri":
                    val = "मिसौरी";
                    break;
                case "Montana":
                    val = "MONTANA";
                    break;
                case "Nebraska":
                    val = "नेब्रास्का";
                    break;
                case "Nevada":
                    val = "नेवादा";
                    break;
                case "New Hampshire":
                    val = "न्यू हैम्पशायर";
                    break;
                case "New Jersey":
                    val = "नयी जर्सी";
                    break;
                case "New Mexico":
                    val = "न्यू मैक्सिको";
                    break;
                case "New York":
                    val = "न्यूयॉर्क";
                    break;
                case "North Carolina":
                    val = "उत्तरी केरोलिना";
                    break;
                case "North Dakota":
                    val = "नॉर्थ डकोटा";
                    break;
                case "Ohio":
                    val = "ओहायो";
                    break;
                case "Oklahoma":
                    val = "ओकलाहोमा";
                    break;
                case "Oregon":
                    val = "ओरेगन";
                    break;
                case "Pennsylvania":
                    val = "पेंसिल्वेनिया";
                    break;
                case "Rhode Island":
                    val = "रोड आइलैंड";
                    break;
                case "South Carolina":
                    val = "दक्षिण कैरोलिना";
                    break;
                case "South Dakota":
                    val = "दक्षिणी डकोटा";
                    break;
                case "Tennessee":
                    val = "टेनेसी";
                    break;
                case "Texas":
                    val = "टेक्सास";
                    break;
                case "Utah":
                    val = "यूटा";
                    break;
                case "Vermont":
                    val = "वरमोंट";
                    break;
                case "Virginia":
                    val = "वर्जीनिया";
                    break;
                case "Washington":
                    val = "वाशिंगटन";
                    break;
                case "West Virginia":
                    val = "वेस्ट वर्जीनिया";
                    break;
                case "Wisconsin":
                    val = "विस्कॉन्सिन";
                    break;
                case "Wyoming":
                    val = "व्योमिंग";
                    break;

                //philipins

                case "Bukidnon":
                    val = "बुकिद्नोन";
                    break;
            }

        } else if (locale.equalsIgnoreCase("te")) {
            switch (val) {

                case "Andhra Pradesh":
                    val = "రాష్ట్రం ఎంచుకోండి";
                    break;
                case "Arunachal Pradesh":
                    val = "అలబామా";
                    break;
                case "Assam Bihar":
                    val = "అలాస్కా";
                    break;
                case "Chhattisgarh Goa":
                    val = "అరిజోనా";
                    break;
                case "Gujarat Haryana":
                    val = "అర్కాన్సాస్";
                    break;
                case "Himachal Pradesh":
                    val = "కాలిఫోర్నియా";
                    break;
                case "Jammu  Kashmir":
                    val = "కొలరాడో";
                    break;
                case "Jharkhand":
                    val = "కనెక్టికట్";
                    break;
                case "Karnataka":
                    val = "డెలావేర్";
                    break;
                case "Kerala":
                    val = "ఫ్లోరిడా";
                    break;
                case "Madhya Pradesh":
                    val = "జార్జియా";
                    break;
                case "Maharashtra":
                    val = "హవాయి";
                    break;
                case "Manipur":
                    val = "ఇడాహో";
                    break;
                case "Meghalaya":
                    val = "ఇల్లినాయిస్";
                    break;
                case "Mizoram":
                    val = "ఇండియానా";
                    break;
                case "Nagaland":
                    val = "అయోవా";
                    break;
                case "Odisha":
                    val = "కాన్సాస్";
                    break;
                case "Punjab":
                    val = "కెంటుకీ";
                    break;
                case "Rajasthan":
                    val = "లూసియానా";
                    break;
                case "Sikkim":
                    val = "మైనే";
                    break;
                case "Tamil Nadu":
                    val = "మేరీల్యాండ్";
                    break;
                case "Telangana":
                    val = "మసాచుసెట్స్";
                    break;
                case "Tripura":
                    val = "మిచిగాన్";
                    break;
                case "Uttar Pradesh":
                    val = "మిన్నెసోటా";
                    break;
                case "Uttarakhand":
                    val = "మిసిసిపీ";
                    break;
                case "West Bengal":
                    val = "మిస్సౌరీ";
                    break;
                case "Alabama":
                    val = "అలబామా";
                    break;
                case "Alaska":
                    val = "అలాస్కా";
                    break;
                case "Arizona":
                    val = "అరిజోనా";
                    break;
                case "Arkansas":
                    val = "అర్కాన్సాస్";
                    break;
                case "California":
                    val = "కాలిఫోర్నియా";
                    break;
                case "Colorado":
                    val = "కొలరాడో";
                    break;
                case "Connecticut":
                    val = "కనెక్టికట్";
                    break;
                case "Delaware":
                    val = "డెలావేర్";
                    break;
                case "Florida":
                    val = "ఫ్లోరిడా";
                    break;
                case "Georgia":
                    val = "జార్జియా";
                    break;
                case "Hawaii":
                    val = "హవాయి";
                    break;
                case "Idaho":
                    val = "ఇడాహో";
                    break;
                case "Illinois":
                    val = "ఇల్లినాయిస్";
                    break;
                case "Indiana":
                    val = "ఇండియానా";
                    break;
                case "Iowa":
                    val = "అయోవా";
                    break;
                case "Kansas":
                    val = "కాన్సాస్";
                    break;
                case "Kentucky":
                    val = "కెంటుకీ";
                    break;
                case "Louisiana":
                    val = "లూసియానా";
                    break;
                case "Maine":
                    val = "మైనే";
                    break;
                case "Maryland":
                    val = "మేరీల్యాండ్";
                    break;
                case "Massachusetts":
                    val = "మసాచుసెట్స్";
                    break;
                case "Michigan":
                    val = "మిచిగాన్";
                    break;
                case "Minnesota":
                    val = "మిన్నెసోటా";
                    break;
                case "Mississippi":
                    val = "మిసిసిపీ";
                    break;
                case "Missouri":
                    val = "మిస్సౌరీ";
                    break;
                case "Montana":
                    val = "మోంటానా";
                    break;
                case "Nebraska":
                    val = "నెబ్రాస్కా";
                    break;
                case "Nevada":
                    val = "నెవాడా";
                    break;
                case "New Hampshire":
                    val = "న్యూ హాంప్షైర్";
                    break;
                case "New Jersey":
                    val = "కొత్త కోటు";
                    break;
                case "New Mexico":
                    val = "న్యూ మెక్సికో";
                    break;
                case "New York":
                    val = "న్యూయార్క్";
                    break;
                case "North Carolina":
                    val = "ఉత్తర కరొలినా";
                    break;
                case "North Dakota":
                    val = "ఉత్తర డకోటా";
                    break;
                case "Ohio":
                    val = "ఒహియో";
                    break;
                case "Oklahoma":
                    val = "ఓక్లహోమా";
                    break;
                case "Oregon":
                    val = "ఒరెగాన్";
                    break;
                case "Pennsylvania":
                    val = "పెన్సిల్వేనియా";
                    break;
                case "Rhode Island":
                    val = "రోడ్ దీవి";
                    break;
                case "South Carolina":
                    val = "దక్షిణ కరోలినా";
                    break;
                case "South Dakota":
                    val = "దక్షిణ డకోటా";
                    break;
                case "Tennessee":
                    val = "టేనస్సీ";
                    break;
                case "Texas":
                    val = "టెక్సాస్";
                    break;
                case "Utah":
                    val = "ఉతా";
                    break;
                case "Vermont":
                    val = "వెర్మోంట్";
                    break;
                case "Virginia":
                    val = "వర్జీనియా";
                    break;
                case "Washington":
                    val = "వాషింగ్టన్";
                    break;
                case "West Virginia":
                    val = "వెస్ట్ వర్జీనియా";
                    break;
                case "Wisconsin":
                    val = "విస్కాన్సిన్";
                    break;
                case "Wyoming":
                    val = "వ్యోమింగ్";
                    break;

                default:
                    return val;

            }
        } else {
            return val;
        }
        return val;
    }

    public static String switch_hi_en_dictrict(String val, String locale) {
        if (locale.equalsIgnoreCase("hi")) {
            switch (val) {
                case "Andhra Pradesh":
                    val = "आंध्र प्रदेश";
                    break;
                case "Arunachal Pradesh":
                    val = "अरुणाचल प्रदेश";
                    break;
                case "Assam":
                    val = "असम";
                    break;
                case "Bihar":
                    val = "बिहार";
                    break;
                case "Chhattisgarh":
                    val = "छत्तीसगढ";
                    break;
                case "Goa":
                    val = "गोवा";
                    break;
                case "Gujarat":
                    val = "गुजरात";
                    break;
                case "Haryana":
                    val = "हरयाणा";
                    break;
                case "Himachal Pradesh":
                    val = "हिमाचल प्रदेश";
                    break;
                case "Jammu &amp; Kashmir":
                    val = "जम्मू कश्मीर";
                    break;
                case "Jharkhand":
                    val = "झारखंड";
                    break;
                case "Karnataka":
                    val = "कर्नाटक";
                    break;
                case "Kerala":
                    val = "केरल";
                    break;
                case "Madhya Pradesh":
                    val = "मध्य प्रदेश";
                    break;
                case "Maharashtra":
                    val = "महाराष्ट्र";
                    break;
                case "Manipur":
                    val = "मणिपुर";
                    break;
                case "Meghalaya":
                    val = "मेघालय";
                    break;
                case "Mizoram":
                    val = "मिजोरम";
                    break;
                case "Nagaland":
                    val = "नगालैंड";
                    break;
                case "Odisha":
                    val = "उड़ीसा";
                    break;
                case "Punjab":
                    val = "पंजाब";
                    break;
                case "Rajasthan":
                    val = "राजस्थान";
                    break;
                case "Sikkim":
                    val = "सिक्किम";
                    break;
                case "Tamil Nadu":
                    val = "तमिलनाडु";
                    break;
                case "Telangana":
                    val = "तेलंगाना";
                    break;
                case "Tripura":
                    val = "त्रिपुरा";
                    break;
                case "Uttar Pradesh":
                    val = "उत्तर प्रदेश";
                    break;
                case "Uttarakhand":
                    val = "उत्तराखंड";
                    break;
                case "West Bengal":
                    val = "पश्चिम बंगाल";
                    break;
            }

        } else {
            return val;
        }
        return val;
    }

    /**
     * Ritika======
     * get only selective english Language from user input and insert into  Db
     *
     * @param val    db Values in en locals only
     * @param locale selective app language locale.
     * @return return String value according the Locals to show on CountrySpinner.
     */

    public static String mSwitch_hi_en_te_State(String val, String locale) {

        switch (val) {
            //telugu us state
            case "అలబామా":
                val = "Alabama";
                break;
            case "అలాస్కా":
                val = "Alaska";
                break;
            case "అరిజోనా":
                val = "Arizona";
                break;
            case "అర్కాన్సాస్":
                val = "Arkansas";
                break;
            case "కాలిఫోర్నియా":
                val = "California";
                break;
            case "కొలరాడో":
                val = "Colorado";
                break;
            case "కనెక్టికట్":
                val = "Connecticut";
                break;
            case "డెలావేర్":
                val = "Delaware";
                break;
            case "ఫ్లోరిడా":
                val = "Florida";
                break;
            case "జార్జియా":
                val = "Georgia";
                break;
            case "హవాయి":
                val = "Hawaii";
                break;
            case "ఇడాహో":
                val = "Idaho";
                break;
            case "ఇల్లినాయిస్":
                val = "Illinois";
                break;
            case "ఇండియానా":
                val = "Indiana";
                break;
            case "అయోవా":
                val = "Iowa";
                break;
            case "కాన్సాస్":
                val = "Kansas";
                break;
            case "కెంటుకీ":
                val = "Kentucky";
                break;
            case "లూసియానా":
                val = "Louisiana";
                break;
            case "మైనే":
                val = "Maine";
                break;
            case "మేరీల్యాండ్":
                val = "Maryland";
                break;
            case "మసాచుసెట్స్":
                val = "Massachusetts";
                break;
            case "మిచిగాన్":
                val = "Michigan";
                break;
            case "మిన్నెసోటా":
                val = "Minnesota";
                break;
            case "మిసిసిపీ":
                val = "Mississippi";
                break;
            case "మిస్సౌరీ":
                val = "Missouri";
                break;
            case "మోంటానా":
                val = "Montana";
                break;
            case "నెబ్రాస్కా":
                val = "Nebraska";
                break;
            case "నెవాడా":
                val = "Nevada";
                break;
            case "న్యూ హాంప్షైర్":
                val = "New Hampshire";
                break;
            case "కొత్త కోటు":
                val = "New Jersey";
                break;
            case "న్యూ మెక్సికో":
                val = "New Mexico";
                break;
            case "న్యూయార్క్":
                val = "New York";
                break;
            case "ఉత్తర కరొలినా":
                val = "North Carolina";
                break;
            case "ఉత్తర డకోటా":
                val = "North Dakota";
                break;
            case "ఒహియో":
                val = "Ohio";
                break;
            case "ఓక్లహోమా":
                val = "Oklahoma";
                break;
            case "ఒరెగాన్":
                val = "Oregon";
                break;
            case "పెన్సిల్వేనియా":
                val = "Pennsylvania";
                break;
            case "రోడ్ దీవి":
                val = "Rhode Island";
                break;
            case "దక్షిణ కరోలినా":
                val = "South Carolina";
                break;
            case "దక్షిణ డకోటా":
                val = "South Dakota";
                break;
            case "టేనస్సీ":
                val = "Tennessee";
                break;
            case "టెక్సాస్":
                val = "Texas";
                break;
            case "ఉతా":
                val = "Utah";
                break;
            case "వెర్మోంట్":
                val = "Vermont";
                break;
            case "వర్జీనియా":
                val = "Virginia";
                break;
            case "వాషింగ్టన్":
                val = "Washington";
                break;
            case "వెస్ట్ వర్జీనియా":
                val = "West Virginia";
                break;
            case "విస్కాన్సిన్":
                val = "Wisconsin";
                break;
            case "వ్యోమింగ్":
                val = "Wyoming";
                break;


            case "अलाबामा":
                val = "Alabama";
                break;
            case "अलास्का":
                val = "Alaska";
                break;
            case "एरिज़ोना":
                val = "Arizona";
                break;
            case "अर्कांसासो":
                val = "Arkansas";
                break;
            case "कैलिफोर्निया":
                val = "California";
                break;
            case "कोलोराडो":
                val = "Colorado";
                break;
            case "कनेक्टिकट":
                val = "Connecticut";
                break;
            case "डेलावेयर":
                val = "Delaware";
                break;
            case "फ्लोरिडा":
                val = "Florida";
                break;
            case "जॉर्जिया":
                val = "Georgia";
                break;
            case "हवाई":
                val = "Hawaii";
                break;
            case "इडाहो":
                val = "Idaho";
                break;
            case "इलिनोइस":
                val = "Illinois";
                break;
            case "इंडियाना":
                val = "Indiana";
                break;
            case "आयोवा":
                val = "Iowa";
                break;
            case "कान्सास":
                val = "Kansas";
                break;
            case "केंटकी":
                val = "Kentucky";
                break;
            case "लुइसियाना":
                val = "Louisiana";
                break;
            case "मैंने":
                val = "Maine";
                break;
            case "मैरीलैंड":
                val = "Maryland";
                break;
            case "मैसाचुसेट्स":
                val = "Massachusetts";
                break;
            case "मिशिगन":
                val = "Michigan";
                break;
            case "मिनेसोटा":
                val = "Minnesota";
                break;
            case "मिसीसिपी":
                val = "Mississippi";
                break;
            case "मिसौरी":
                val = "Missouri";
                break;
            case "MONTANA":
                val = "Montana";
                break;
            case "नेब्रास्का":
                val = "Nebraska";
                break;
            case "नेवादा":
                val = "Nevada";
                break;
            case "न्यू हैम्पशायर":
                val = "New Hampshire";
                break;
            case "नयी जर्सी":
                val = "New Jersey";
                break;
            case "न्यू मैक्सिको":
                val = "New Mexico";
                break;
            case "न्यूयॉर्क":
                val = "New York";
                break;
            case "उत्तरी केरोलिना":
                val = "North Carolina";
                break;
            case "नॉर्थ डकोटा":
                val = "North Dakota";
                break;
            case "ओहायो":
                val = "Ohio";
                break;
            case "ओकलाहोमा":
                val = "Oklahoma";
                break;
            case "ओरेगन":
                val = "Oregon";
                break;
            case "पेंसिल्वेनिया":
                val = "Pennsylvania";
                break;
            case "रोड आइलैंड":
                val = "Rhode Island";
                break;
            case "दक्षिण कैरोलिना":
                val = "South Carolina";
                break;
            case "दक्षिणी डकोटा":
                val = "South Dakota";
                break;
            case "टेनेसी":
                val = "Tennessee";
                break;
            case "टेक्सास":
                val = "Texas";
                break;
            case "यूटा":
                val = "Utah";
                break;
            case "वरमोंट":
                val = "Vermont";
                break;
            case "वर्जीनिया":
                val = "Virginia";
                break;
            case "वाशिंगटन":
                val = "Washington";
                break;
            case "वेस्ट वर्जीनिया":
                val = "West Virginia";
                break;
            case "विस्कॉन्सिन":
                val = "Wisconsin";
                break;
            case "व्योमिंग":
                val = "Wyoming";
                break;
            // philipins state
            case "బుకిడ్నాన్":
                val = "Bukidnon";
                break;

            case "बुकिद्नोन":
                val = "Bukidnon";
                break;

            //indian state According
            case "ఆంధ్రప్రదేశ్":
                val = "Andhra Pradesh";
                break;
            case "అరుణాచల్ ప్రదేశ్":
                val = "Arunachal Pradesh";
                break;
            case "అస్సాం":
                val = "Assam";
                break;
            case "బీహార్":
                val = "Bihar";
                break;
            case "ఛత్తీస్‌గ h ్":
                val = "Chhattisgarh";
                break;
            case "గోవా":
                val = "Goa";
                break;
            case "గుజరాత్":
                val = "Gujarat";
                break;
            case "హర్యానా":
                val = "Haryana";
                break;
            case "హిమాచల్ ప్రదేశ్":
                val = "Himachal Pradesh";
                break;
            case "జమ్మూ కాశ్మీర్":
                val = "Jammu &amp; Kashmir";
                break;
            case "జార్ఖండ్":
                val = "Jharkhand";
                break;
            case "కర్ణాటక":
                val = "Karnataka";
                break;
            case "కేరళ":
                val = "Kerala";
                break;
            case "మధ్యప్రదేశ్":
                val = "Madhya Pradesh";
                break;
            case "మహారాష్ట్ర":
                val = "Maharashtra";
                break;
            case "మణిపూర్":
                val = "Manipur";
                break;
            case "మేఘాలయ":
                val = "Meghalaya";
                break;
            case "మిజోరం":
                val = "Mizoram";
                break;
            case "నాగాలాండ్":
                val = "Nagaland";
                break;
            case "ఒడిశా":
                val = "Odisha";
                break;
            case "పంజాబ్":
                val = "Punjab";
                break;
            case "రాజస్థాన్":
                val = "Rajasthan";
                break;
            case "సిక్కిం":
                val = "Sikkim";
                break;
            case "తమిళనాడు":
                val = "Tamil Nadu";
                break;
            case "తెలంగాణ":
                val = "Telangana";
                break;
            case "త్రిపుర":
                val = "Tripura";
                break;
            case "ఉత్తర ప్రదేశ్":
                val = "Uttar Pradesh";
                break;
            case "ఉత్తరాఖండ్":
                val = "Uttarakhand";
                break;
            case "పశ్చిమ బెంగాల్":
                val = "West Bengal";
                break;

            //hindi india state

            case "आंध्र प्रदेश":
                val = "Andhra Pradesh";
                break;
            case "अरुणाचल प्रदेश":
                val = "Arunachal Pradesh";
                break;
            case "असम":
                val = "Assam";
                break;
            case "बिहार":
                val = "Bihar";
                break;
            case "छत्तीसगढ":
                val = "Chhattisgarh";
                break;
            case "गोवा":
                val = "Goa";
                break;
            case "गुजरात":
                val = "Gujarat";
                break;
            case "हरयाणा":
                val = "Haryana";
                break;
            case "हिमाचल प्रदेश":
                val = "Himachal Pradesh";
                break;
            case "जम्मू कश्मीर":
                val = "Jammu &amp; Kashmir";
                break;
            case "झारखंड":
                val = "Jharkhand";
                break;
            case "कर्नाटक":
                val = "Karnataka";
                break;
            case "केरल":
                val = "Kerala";
                break;
            case "मध्य प्रदेश":
                val = "Madhya Pradesh";
                break;
            case "महाराष्ट्र":
                val = "Maharashtra";
                break;
            case "मणिपुर":
                val = "Manipur";
                break;
            case "मेघालय":
                val = "Meghalaya";
                break;
            case "मिजोरम":
                val = "Mizoram";
                break;
            case "नगालैंड":
                val = "Nagaland";
                break;
            case "उड़ीसा":
                val = "Odisha";
                break;
            case "पंजाब":
                val = "Punjab";
                break;
            case "राजस्थान":
                val = "Rajasthan";
                break;
            case "सिक्किम":
                val = "Sikkim";
                break;
            case "तमिलनाडु":
                val = "Tamil Nadu";
                break;
            case "तेलंगाना":
                val = "Telangana";
                break;
            case "त्रिपुरा":
                val = "Tripura";
                break;
            case "उत्तर प्रदेश":
                val = "Uttar Pradesh";
                break;
            case "उत्तराखंड":
                val = "Uttarakhand";
                break;
            case "पश्चिम बंगाल":
                val = "West Bengal";
                break;

            default:
                return val;

        }
        return val;
    }

    public static String getTranslatedDays(String val, String locale) {
        if (val == null || val.isEmpty()) return "";

        if (locale.equalsIgnoreCase("as")) {
            switch (val) {
                case "Sunday":
                    val = "দেওবাৰ";
                    break;
                case "Monday":
                    val = "সোমবাৰ";
                    break;
                case "Tuesday":
                    val = "মংগলবাৰ";
                    break;
                case "Wednesday":
                    val = "বুধবাৰ";
                    break;
                case "Thursday":
                    val = "বৃহস্পতিবাৰ";
                    break;
                case "Friday":
                    val = "শুক্ৰবাৰ";
                    break;
                case "Saturday":
                    val = "শনিবাৰ";
                    break;
                default:
                    return val;
            }

        } else if (locale.equalsIgnoreCase("bn")) {
            switch (val) {
                case "Sunday":
                    val = "রবিবার";
                    break;
                case "Monday":
                    val = "সোমবার";
                    break;
                case "Tuesday":
                    val = "মঙ্গলবার";
                    break;
                case "Wednesday":
                    val = "বুধবার";
                    break;
                case "Thursday":
                    val = "বৃহস্পতিবার";
                    break;
                case "Friday":
                    val = "শুক্রবার";
                    break;
                case "Saturday":
                    val = "শনিবার";
                    break;
                default:
                    return val;
            }

        } else if (locale.equalsIgnoreCase("gu")) {
            switch (val) {
                case "Sunday":
                    val = "રવિવાર";
                    break;
                case "Monday":
                    val = "સોમવાર";
                    break;
                case "Tuesday":
                    val = "મંગળવારે";
                    break;
                case "Wednesday":
                    val = "બુધવાર";
                    break;

                case "Thursday":
                    val = "ગુરુવાર";
                    break;
                case "Friday":
                    val = "શુક્રવાર";
                    break;
                case "Saturday":
                    val = "શનિવાર";
                    break;
                default:
                    return val;
            }

        } else if (locale.equalsIgnoreCase("hi")) {
            switch (val) {
                case "Sunday":
                    val = "रविवार";
                    break;
                case "Monday":
                    val = "सोमवार";
                    break;
                case "Tuesday":
                    val = "मंगलवार";
                    break;
                case "Wednesday":
                    val = "बुधवार";
                    break;
                case "Thursday":
                    val = "गुरूवार";
                    break;
                case "Friday":
                    val = "शुक्रवार";
                    break;
                case "Saturday":
                    val = "शनिवार";
                    break;
                default:
                    return val;
            }

        } else if (locale.equalsIgnoreCase("kn")) {
            switch (val) {
                case "Sunday":
                    val = "ಭಾನುವಾರ";
                    break;
                case "Monday":
                    val = "ಸೋಮವಾರ";
                    break;
                case "Tuesday":
                    val = "ಮಂಗಳವಾರ";
                    break;
                case "Wednesday":
                    val = "ಬುಧವಾರ";
                    break;

                case "Thursday":
                    val = "ಗುರುವಾರ";
                    break;
                case "Friday":
                    val = "ಶುಕ್ರವಾರ";
                    break;
                case "Saturday":
                    val = "ಶನಿವಾರ";
                    break;
                default:
                    return val;
            }

        } else if (locale.equalsIgnoreCase("ml")) {
            switch (val) {
                case "Sunday":
                    val = "ഞായറാഴ്ച";
                    break;
                case "Monday":
                    val = "തിങ്കളാഴ്ച";
                    break;
                case "Tuesday":
                    val = "ചൊവ്വാഴ്ച";
                    break;
                case "Wednesday":
                    val = "ബുധനാഴ്ച";
                    break;

                case "Thursday":
                    val = "വ്യാഴാഴ്ച";
                    break;
                case "Friday":
                    val = "വെള്ളിയാഴ്ച";
                    break;
                case "Saturday":
                    val = "ശനിയാഴ്ച";
                    break;
                default:
                    return val;
            }

        } else if (locale.equalsIgnoreCase("mr")) {
            switch (val) {
                case "Sunday":
                    val = "रविवार";
                    break;
                case "Monday":
                    val = "सोमवार";
                    break;
                case "Tuesday":
                    val = "मंगळवार";
                    break;
                case "Wednesday":
                    val = "बुधवार";
                    break;

                case "Thursday":
                    val = "गुरुवार";
                    break;
                case "Friday":
                    val = "शुक्रवार";
                    break;
                case "Saturday":
                    val = "शनिवार";
                    break;
                default:
                    return val;
            }

        } else if (locale.equalsIgnoreCase("or")) {
            switch (val) {
                case "Sunday":
                    val = "ରବିବାର";
                    break;
                case "Monday":
                    val = "ସୋମବାର";
                    break;
                case "Tuesday":
                    val = "ମଙ୍ଗଳବାର";
                    break;
                case "Wednesday":
                    val = "ବୁଧବାର";
                    break;

                case "Thursday":
                    val = "ଗୁରୁବାର";
                    break;
                case "Friday":
                    val = "ଶୁକ୍ରବାର";
                    break;
                case "Saturday":
                    val = "ଶନିବାର";
                    break;
                default:
                    return val;
            }

        } else if (locale.equalsIgnoreCase("ru")) {
            switch (val) {
                case "Sunday":
                    val = "Воскресенье";
                    break;
                case "Monday":
                    val = "понедельник";
                    break;
                case "Tuesday":
                    val = "вторник";
                    break;
                case "Wednesday":
                    val = "среда";
                    break;

                case "Thursday":
                    val = "Четверг";
                    break;
                case "Friday":
                    val = "Пятница";
                    break;
                case "Saturday":
                    val = "Суббота";
                    break;
                default:
                    return val;
            }

        } else if (locale.equalsIgnoreCase("ta")) {
            switch (val) {
                case "Sunday":
                    val = "ஞாயிற்றுக்கிழமை";
                    break;
                case "Monday":
                    val = "திங்கட்கிழமை";
                    break;
                case "Tuesday":
                    val = "செவ்வாய்";
                    break;
                case "Wednesday":
                    val = "புதன்";
                    break;

                case "Thursday":
                    val = "வியாழன்";
                    break;
                case "Friday":
                    val = "வெள்ளி";
                    break;
                case "Saturday":
                    val = "சனிக்கிழமை";
                    break;
                default:
                    return val;
            }

        } else if (locale.equalsIgnoreCase("te")) {

            switch (val) {
                case "Sunday":
                    val = "ఆదివారం";
                    break;
                case "Monday":
                    val = "సోమవారం";
                    break;
                case "Tuesday":
                    val = "మంగళవారం";
                    break;
                case "Wednesday":
                    val = "బుధవారం";
                    break;

                case "Thursday":
                    val = "గురువారం";
                    break;
                case "Friday":
                    val = "శుక్రవారం";
                    break;
                case "Saturday":
                    val = "శనివారం";
                    break;
                default:
                    return val;
            }

        } else {
            return val;
        }
        return val;
    }

    public static String getAppointmentBookStatus(String val, String locale) {

        if (locale.equalsIgnoreCase("ru")) {
            switch (val.toLowerCase()) {
                case "booked":
                    val = "забронировано";
                    break;
                case "canceled":
                    val = "отменен";
                    break;
                case "visit closed":
                    val = "отменен";
                    break;
                default:
                    return val;
            }
        } else if (locale.equalsIgnoreCase("as")) {

            switch (val.toLowerCase()) {
                case "booked":
                    val = "বুক কৰা হৈছে";
                    break;
                case "canceled":
                    val = "বাতিল কৰা হৈছে";
                    break;
                case "visit closed":
                    val = "সাক্ষাৎ বন্ধ";
                    break;
                default:
                    return val;
            }
        } else if (locale.equalsIgnoreCase("bn")) {
            switch (val.toLowerCase()) {
                case "booked":
                    val = "বুক করা";
                    break;
                case "canceled":
                    val = "বাতিল";
                    break;
                case "visit closed":
                    val = "পরিদর্শন বন্ধ";
                    break;
                default:
                    return val;
            }
        } else if (locale.equalsIgnoreCase("gu")) {
            switch (val.toLowerCase()) {
                case "booked":
                    val = "બુક કરેલ";
                    break;
                case "canceled":
                    val = "રદ કરેલ";
                    break;
                case "visit closed":
                    val = "મુલાકાત બંધ";
                    break;
                default:
                    return val;
            }
        } else if (locale.equalsIgnoreCase("hi")) {

            switch (val.toLowerCase()) {
                case "booked":
                    val = "बुक";
                    break;
                case "canceled":
                    val = "रद्द";
                    break;
                case "visit closed":
                    val = "यात्रा बंद";
                    break;
                default:
                    return val;
            }
        } else if (locale.equalsIgnoreCase("kn")) {
            switch (val.toLowerCase()) {
                case "booked":
                    val = "ಬುಕ್ ಮಾಡಿದೆ";
                    break;
                case "canceled":
                    val = "ರದ್ದುಗೊಳಿಸಲಾಗಿದೆ";
                    break;
                case "visit closed":
                    val = "ಭೇಟಿ ಮುಚ್ಚಲಾಗಿದೆ";
                    break;
                default:
                    return val;
            }
        } else if (locale.equalsIgnoreCase("ml")) {
            switch (val.toLowerCase()) {
                case "booked":
                    val = "ബുക്ക് ചെയ്തു";
                    break;
                case "canceled":
                    val = "റദ്ദാക്കി";
                    break;
                case "visit closed":
                    val = "സന്ദർശനം അടച്ചു";
                    break;
                default:
                    return val;
            }
        } else if (locale.equalsIgnoreCase("mr")) {
            switch (val.toLowerCase()) {
                case "booked":
                    val = "बुक केले";
                    break;
                case "canceled":
                    val = "रद्द केले";
                    break;
                case "visit closed":
                    val = "भेट बंद";
                    break;
                default:
                    return val;
            }
        } else if (locale.equalsIgnoreCase("or")) {
            switch (val.toLowerCase()) {


                case "booked":
                    val = "ବୁକ୍ ହୋଇଛି";
                    break;
                case "canceled":
                    val = "ବାତିଲ";
                    break;
                case "visit closed":
                    val = "ପରିଦର୍ଶନ ବନ୍ଦ";
                    break;
                default:
                    return val;
            }
        } else if (locale.equalsIgnoreCase("ta")) {
            switch (val.toLowerCase()) {
                case "booked":
                    val = "பதிவு செய்யப்பட்டது";
                    break;
                case "canceled":
                    val = "ரத்து செய்யப்பட்டது";
                    break;
                case "visit closed":
                    val = "வருகை மூடப்பட்டது";
                    break;
                default:
                    return val;


            }
        } else if (locale.equalsIgnoreCase("te")) {
            switch (val.toLowerCase()) {
                case "booked":
                    val = "బుక్ చేసుకున్నారు";
                    break;
                case "cancelled":
                    val = "రద్దు";
                    break;
                case "visit closed":
                    val = "సందర్శన మూసివేయబడింది";
                    break;
                default:
                    return val;
            }
        } else {
            return val;
        }
        return val;
    }

    public static String switch_hi_guardian_type(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "मां" -> val = "Mother";
            case "पिता" -> val = "Father";
            case "दादा-दादी" -> val = "Grandparent";
            case "पति/पत्नी" -> val = "Spouse";
            default -> {
                return val;
            }
        }
        return val;
    }

    public static String switch_hi_guardian_type_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Mother" -> val = "मां";
            case "Father" -> val = "पिता";
            case "Grandparent" -> val = "दादा-दादी";
            case "Spouse" -> val = "पति/पत्नी";
            default -> {
                return val;
            }
        }
        return val;
    }

    public static String switch_hi_contact_type(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "स्वयं" -> val = "Self";
            case "परिवार" -> val = "Family";
            default -> {
                return val;
            }
        }
        return val;
    }

    public static String switch_hi_contact_type_edit(String val) {
        if (val == null || val.isEmpty()) return "";
        switch (val) {
            case "Self" -> val = "स्वयं";
            case "Family" -> val = "परिवार";
            default -> {
                return val;
            }
        }
        return val;
    }

    public static InputFilter inputFilter_Name = new InputFilter() { //filter input for name fields
        @Override
        public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned, int i2, int i3) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = charSequence.charAt(i);
                if (isCharAllowed(c)) // put your condition here
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return null;
            else {
                if (charSequence instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) charSequence, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

        private boolean isCharAllowed(char c) {
            return Character.isLetterOrDigit(c) || Character.isSpaceChar(c);    // This allows only number and alphabets.
        }
    };

    public static InputFilter inputFilter_Others = new InputFilter() { //filter input for all other fields
        @Override
        public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned, int i2, int i3) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = charSequence.charAt(i);
                if (isCharAllowed(c)) // put your condition here
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return null;
            else {
                if (charSequence instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) charSequence, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

        private boolean isCharAllowed(char c) {
            //   return Character.isLetterOrDigit(c) || Character.isSpaceChar(c);
            return Character.isLetter(c) || Character.isSpaceChar(c);   // This allows only alphabets.
        }
    };

    public static InputFilter inputFilter_SearchBar = new InputFilter() { //filter input for all other fields
        @Override
        public CharSequence filter(CharSequence charSequence, int start, int end, Spanned spanned, int i2, int i3) {
            boolean keepOriginal = true;
            StringBuilder sb = new StringBuilder(end - start);
            for (int i = start; i < end; i++) {
                char c = charSequence.charAt(i);
                if (isCharAllowed(c)) // put your condition here
                    sb.append(c);
                else
                    keepOriginal = false;
            }
            if (keepOriginal)
                return null;
            else {
                if (charSequence instanceof Spanned) {
                    SpannableString sp = new SpannableString(sb);
                    TextUtils.copySpansFrom((Spanned) charSequence, start, sb.length(), null, sp, 0);
                    return sp;
                } else {
                    return sb;
                }
            }
        }

        private boolean isCharAllowed(char c) {
            return Character.isLetterOrDigit(c) || Character.valueOf(c).equals('-') || Character.valueOf(c).equals(' ');
        }
    };

    public static String formatDoubleValues(Double value) {
        DecimalFormat df = new DecimalFormat("0.0");
        df.setRoundingMode(RoundingMode.HALF_UP);
        return String.valueOf(df.format(value));
    }

    /**
     * Setting local language for Gender data.
     *
     * @param context
     * @param genderView
     * @param patient
     * @param sessionManager
     */
    public static void setGenderAgeLocal(Context context, TextView genderView, String dob, String gender, SessionManager sessionManager) {
        //  1. Age
        String age = DateAndTimeUtils.getAge_FollowUp(dob, context);

        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + " " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + " " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + " " + age);
            } else {
                genderView.setText(gender + " " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + " " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + " " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + " " + age);
            } else {
                genderView.setText(gender + " " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + " " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + " " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + " " + age);
            } else {
                genderView.setText(gender + " " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + " " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + " " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + " " + age);
            } else {
                genderView.setText(gender + " " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + " " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + " " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + " " + age);
            } else {
                genderView.setText(gender + " " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + " " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + " " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + " " + age);
            } else {
                genderView.setText(gender + " " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + " " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + " " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + " " + age);
            } else {
                genderView.setText(gender + " " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + " " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + " " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + " " + age);
            } else {
                genderView.setText(gender + " " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + " " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + " " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + " " + age);
            } else {
                genderView.setText(gender + " " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + " " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + " " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + " " + age);
            } else {
                genderView.setText(gender + " " + age);
            }
        } else {
            genderView.setText(gender + " " + age);
        }
    }

    /**
     * returning string instead of setting data to textview
     * @param context
     * @param dob
     * @param gender
     * @return
     */
    public static String setGenderAgeLocal(Context context, String dob, String gender) {
        String age = DateAndTimeUtils.getAge_FollowUp(dob, context);

        int genderResId;
        if (gender.equalsIgnoreCase("M")) {
            genderResId = R.string.identification_screen_checkbox_male;
        } else if (gender.equalsIgnoreCase("F")) {
            genderResId = R.string.identification_screen_checkbox_female;
        } else if (gender.equalsIgnoreCase("O")) {
            genderResId = R.string.identification_screen_checkbox_other;
        } else {
            return gender + " " + age;
        }

        String localizedGender = context.getResources().getString(genderResId);
        return  localizedGender + " " + age;
    }

    /**
     * Setting local language for Gender data.
     *
     * @param context
     * @param genderView
     * @param patient
     * @param sessionManager
     */
    public static void setGenderAgeLocalByCommaContact(Context context, TextView genderView, String dob, String gender, SessionManager sessionManager) {
        //  1. Age
        String age = DateAndTimeUtils.getAge_FollowUp(dob, context);

        if (sessionManager.getAppLanguage().equalsIgnoreCase("hi")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + ", " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + ", " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + ", " + age);
            } else {
                genderView.setText(gender + ", " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("or")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + ", " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + ", " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + ", " + age);
            } else {
                genderView.setText(gender + ", " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("te")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + ", " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + ", " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + ", " + age);
            } else {
                genderView.setText(gender + ", " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("mr")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + ", " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + ", " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + ", " + age);
            } else {
                genderView.setText(gender + ", " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("as")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + ", " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + ", " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + ", " + age);
            } else {
                genderView.setText(gender + ", " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ml")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + ", " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + ", " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + ", " + age);
            } else {
                genderView.setText(gender + ", " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("gu")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + ", " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + ", " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + ", " + age);
            } else {
                genderView.setText(gender + ", " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("kn")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + ", " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + ", " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + ", " + age);
            } else {
                genderView.setText(gender + ", " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("bn")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + ", " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + ", " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + ", " + age);
            } else {
                genderView.setText(gender + ", " + age);
            }
        } else if (sessionManager.getAppLanguage().equalsIgnoreCase("ta")) {
            if (gender.equalsIgnoreCase("M")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_male) + ", " + age);
            } else if (gender.equalsIgnoreCase("F")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_female) + ", " + age);
            } else if (gender.equalsIgnoreCase("O")) {
                genderView.setText(context.getResources().getString(R.string.identification_screen_checkbox_other) + ", " + age);
            } else {
                genderView.setText(gender + ", " + age);
            }
        } else {
            genderView.setText(gender + ", " + age);
        }
    }

    public static boolean isValidPassword(String passwd) {
        if (passwd == null || passwd.isEmpty()) return false;
        //String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}"; // with special character
        String pattern = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=\\S+$).{8,}"; // without special character
        return passwd.matches(pattern);
    }

    public static String convertCtoF(String temperature) {
        String resultVal;
        NumberFormat nf = NumberFormat.getInstance(Locale.ENGLISH);
        double a = Double.parseDouble(temperature);
        double b = (a * 9 / 5) + 32;
        nf.format(b);
        double roundOff = Math.round(b * 100.0) / 100.0;
        resultVal = nf.format(roundOff);
        return resultVal;
    }

    /*
        public static String getSelectedCheckboxes(ViewGroup viewGroup, String locale, Context context, String otherString) {
            if (viewGroup == null)
                return null;

            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
            configuration.setLocale(new Locale("en"));
            String text = "-";

            JSONArray result = new JSONArray();
            Log.d("devchk", "getSelectedCheckboxes: childcount = "+viewGroup.getChildCount());
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View childAt = viewGroup.getChildAt(i);
                if (childAt instanceof CheckBox) {
                    if (((CheckBox) childAt).isChecked()) {
                        text = ((CheckBox) childAt).getText().toString();

                        if (text.equalsIgnoreCase(context.getString(R.string.other_please_specify)) ||
                                text.equalsIgnoreCase(context.getString(R.string.others)) ||
                                text.equalsIgnoreCase(context.getString(R.string.other_please_specify)) ||
                                text.equalsIgnoreCase(context.getString(R.string.other)) ||
                                text.equalsIgnoreCase(context.getString(R.string.other_specify))
                        ) {
                            text = getSurveyString(text, locale, configuration, context);
                            text = text.concat(" : " + otherString);
                            result.put(text);
                        } else
                            result.put(getSurveyString(text, locale, configuration, context));
                    }
                }
            }
            Log.v("checkbox", "checkboxarray: \n" + result.toString());
            return result.toString();
        }
    */
    public static String getSurveyString(String text, String locale, Configuration configuration, Context context) {
        if (locale.equalsIgnoreCase("mr")) {

            if (context.getString(R.string.sale_of_cereal_production).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.sale_of_cereal_production);

            if (context.getString(R.string.sale_of_animals_or_animal_products).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.sale_of_animals_or_animal_products);

            if (context.getString(R.string.agricultural_wage_labor_employed_for_farm_work).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.agricultural_wage_labor_employed_for_farm_work);

            if (context.getString(R.string.salaried_worker_fixed_monthly_salary).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.salaried_worker_fixed_monthly_salary);

            if (context.getString(R.string.self_employed_non_agricultural_petty_business).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.self_employed_non_agricultural_petty_business);

            if (context.getString(R.string.daily_labor_unskilled_work_agricultural_non_agricultural).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.daily_labor_unskilled_work_agricultural_non_agricultural);

            if (context.getString(R.string.nrega).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.nrega);

            if (context.getString(R.string.seasonal_labor).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.seasonal_labor);

            if (context.getString(R.string.pension).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.pension);

            if (context.getString(R.string.remittances_checkbox).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.remittances_checkbox);

            if (context.getString(R.string.no_paid_work).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.no_paid_work);

            if (context.getString(R.string.other_please_specify).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.other_please_specify);

            if (context.getString(R.string.other_please_specify).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.other_please_specify);

            if (context.getString(R.string.electricity).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.electricity);

            if (context.getString(R.string.lpg_natural_gas).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.lpg_natural_gas);

            if (context.getString(R.string.biogas_checkbox).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.biogas_checkbox);

            if (context.getString(R.string.kerosene).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.kerosene);

            if (context.getString(R.string.coal_lignite).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.coal_lignite);

            if (context.getString(R.string.charcoal).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.charcoal);

            if (context.getString(R.string.wood).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.wood);

            if (context.getString(R.string.straw_shrubs_grass).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.straw_shrubs_grass);

            if (context.getString(R.string.agricultural_crop_waste).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.agricultural_crop_waste);

            if (context.getString(R.string.dung_cakes).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.dung_cakes);

            if (context.getString(R.string.lantern).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.lantern);

            if (context.getString(R.string.kerosene_lamp).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.kerosene_lamp);

            if (context.getString(R.string.candle).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.candle);

            if (context.getString(R.string.electric).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.electric);

            if (context.getString(R.string.lpg).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.lpg);

            if (context.getString(R.string.solar_energy).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.solar_energy);

            if (context.getString(R.string.none).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.none);

            if (context.getString(R.string.piped_into_dwelling).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.piped_into_dwelling);

            if (context.getString(R.string.piped_into_yard_plot).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.piped_into_yard_plot);

            if (context.getString(R.string.public_tap_standpipe).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.public_tap_standpipe);

            if (context.getString(R.string.tube_well_borehole).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.tube_well_borehole);

            if (context.getString(R.string.protected_well_checkbox).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.protected_well_checkbox);

            if (context.getString(R.string.unprotected_well).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.unprotected_well);

            if (context.getString(R.string.protected_spring).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.protected_spring);

            if (context.getString(R.string.unprotected_spring).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.unprotected_spring);

            if (context.getString(R.string.rainwater).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.rainwater);

            if (context.getString(R.string.tanker_truck).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.tanker_truck);

            if (context.getString(R.string.cart_with_small_tank).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.cart_with_small_tank);

            if (context.getString(R.string.surface_water).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.surface_water);

            if (context.getString(R.string.common_hand_pump).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.common_hand_pump);

            if (context.getString(R.string.hand_pump_at_home).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.hand_pump_at_home);

            if (context.getString(R.string.what_do_you_usually_do_to_the_water_to_make_it_safer_to_drink).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.what_do_you_usually_do_to_the_water_to_make_it_safer_to_drink);

            if (context.getString(R.string.boil).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.boil);

            if (context.getString(R.string.use_alum).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.use_alum);

            if (context.getString(R.string.add_bleach_chlorine_tablets_drops).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.add_bleach_chlorine_tablets_drops);

            if (context.getString(R.string.strain_through_a_cloth).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.strain_through_a_cloth);

            if (context.getString(R.string.use_water_filter_ceramic_sand_composite_etc).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.use_water_filter_ceramic_sand_composite_etc);

            if (context.getString(R.string.use_electronic_purifier).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.use_electronic_purifier);

            if (context.getString(R.string.let_it_stand_and_settle).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.let_it_stand_and_settle);

            if (context.getString(R.string.flush_to_piped_sewer_system).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.flush_to_piped_sewer_system);

            if (context.getString(R.string.flush_to_septic_tank).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.flush_to_septic_tank);

            if (context.getString(R.string.flush_to_pit_latrine).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.flush_to_pit_latrine);

            if (context.getString(R.string.flush_to_somewhere_else).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.flush_to_somewhere_else);

            if (context.getString(R.string.flush_dont_know_where).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.flush_dont_know_where);

            if (context.getString(R.string.ventilated_improved_pit_biogas_latrine).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.ventilated_improved_pit_biogas_latrine);

            if (context.getString(R.string.pit_latrine_with_slab).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.pit_latrine_with_slab);

            if (context.getString(R.string.pit_latrine_without_slab_open_pit).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.pit_latrine_without_slab_open_pit);

            if (context.getString(R.string.twin_pit_composting_toilet).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.twin_pit_composting_toilet);

            if (context.getString(R.string.dry_toilet).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.dry_toilet);

            if (context.getString(R.string.communal_toilet).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.communal_toilet);

            if (context.getString(R.string.no_facility_uses_open_space_or_field).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.no_facility_uses_open_space_or_field);

            if (context.getString(R.string.not_treated).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.not_treated);

            if (context.getString(R.string.no_measures_taken_for_purification_drinking_as_it_is).equalsIgnoreCase(text)) {
                return context.createConfigurationContext(configuration).getResources().getString(R.string.no_measures_taken_for_purification_drinking_as_it_is);
            }

            if (context.getString(R.string.starch_staple_food).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.starch_staple_food);

            if (context.getString(R.string.beans_and_peas).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.beans_and_peas);

            if (context.getString(R.string.nuts_and_seeds).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.nuts_and_seeds);

            if (context.getString(R.string.dairy).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.dairy);

            if (context.getString(R.string.eggs).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.eggs);

            if (context.getString(R.string.flesh_food).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.flesh_food);

            if (context.getString(R.string.any_vegetables).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.any_vegetables);

            if (context.getString(R.string.village_tank).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.village_tank);

            if (context.getString(R.string.open_well).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.open_well);

            if (context.getString(R.string.hand_pump_checkbox).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.hand_pump_checkbox);

            if (context.getString(R.string.bore_well).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.bore_well);

            if (context.getString(R.string.river).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.river);

            if (context.getString(R.string.pond).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.pond);

            if (context.getString(R.string.other).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.other);

            if (context.getString(R.string.others).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.others);

            if (context.getString(R.string.other_please_specify).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.other_please_specify);

            if (context.getString(R.string.other_please_specify).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.other_please_specify);

            if (context.getString(R.string.other_specify).equalsIgnoreCase(text))
                return context.createConfigurationContext(configuration).getResources().getString(R.string.other_specify);
        }
        return text;
    }

    public static String getSelectedCheckboxes(ViewGroup viewGroup, String locale, Context context, String otherString) {
        if (viewGroup == null)
            return null;

        Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
        configuration.setLocale(new Locale("en"));
        String text = "-";

        JSONArray result = new JSONArray();
        Log.d("devchk", "getSelectedCheckboxes: Total child count = " + viewGroup.getChildCount());

        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View childAt = viewGroup.getChildAt(i);

            // Log child view type
            Log.d("devchk", "Child " + i + ": Class = " + childAt.getClass().getSimpleName());

            // If the child is a CheckBox, process it as before
            if (childAt instanceof CheckBox) {
                if (((CheckBox) childAt).isChecked()) {
                    text = ((CheckBox) childAt).getText().toString();

                    if (text.equalsIgnoreCase(context.getString(R.string.other_please_specify)) ||
                            text.equalsIgnoreCase(context.getString(R.string.others)) ||
                            text.equalsIgnoreCase(context.getString(R.string.other_please_specify)) ||
                            text.equalsIgnoreCase(context.getString(R.string.other)) ||
                            text.equalsIgnoreCase(context.getString(R.string.other_specify))) {
                        text = getSurveyString(text, locale, configuration, context);
                        text = text.concat(" : " + otherString);
                        result.put(text);
                    } else {
                        result.put(getSurveyString(text, locale, configuration, context));
                    }
                }
            } else {
                // Log extra details for other view types if applicable
                if (childAt instanceof TextView) {
                    Log.d("devchk", "Child " + i + ": Text = " + ((TextView) childAt).getText());
                } else if (childAt instanceof ViewGroup) {
                    Log.d("devchk", "Child " + i + " is a nested ViewGroup with child count = " + ((ViewGroup) childAt).getChildCount());
                } else {
                    Log.d("devchk", "Child " + i + ": Unknown view type");
                }
            }
        }

        Log.v("checkbox", "checkbox array: \n" + result.toString());
        return result.toString();
    }

    public static String getHouseholdHeadReligion(String text, Context context, String locale) {
        Context updatedContext;

        if (locale.equalsIgnoreCase("mr")) {
            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
            configuration.setLocale(new Locale("en"));
            updatedContext = context.createConfigurationContext(configuration);

            if (context.getString(R.string.religion_hindu).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.religion_hindu);
            }

            if (context.getString(R.string.religion_muslim).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.religion_muslim);
            }

            if (context.getString(R.string.religion_christian).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.religion_christian);
            }

            if (context.getString(R.string.religion_sikh).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.religion_sikh);
            }

            if (context.getString(R.string.religion_buddhist_neo_buddhist).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.religion_buddhist_neo_buddhist);
            }

            if (context.getString(R.string.religion_other).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.religion_other);
            }

            if (context.getString(R.string.religion_no_religion).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.religion_no_religion);
            }
        }

        return text;
    }

    public static String getHouseholdHeadReligionEdit(String text, Context context, String locale) {
        Context updatedContext;

        if (locale.equalsIgnoreCase("mr")) {
            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
            configuration.setLocale(new Locale("en"));
            updatedContext = context.createConfigurationContext(configuration);

            if (updatedContext.getString(R.string.religion_hindu).equalsIgnoreCase(text)) {
                return context.getString(R.string.religion_hindu);
            }

            if (updatedContext.getString(R.string.religion_muslim).equalsIgnoreCase(text)) {
                return context.getString(R.string.religion_muslim);
            }

            if (updatedContext.getString(R.string.religion_christian).equalsIgnoreCase(text)) {
                return context.getString(R.string.religion_christian);
            }

            if (updatedContext.getString(R.string.religion_sikh).equalsIgnoreCase(text)) {
                return context.getString(R.string.religion_sikh);
            }

            if (updatedContext.getString(R.string.religion_buddhist_neo_buddhist).equalsIgnoreCase(text)) {
                return context.getString(R.string.religion_buddhist_neo_buddhist);
            }

            if (updatedContext.getString(R.string.religion_other).equalsIgnoreCase(text)) {
                return context.getString(R.string.religion_other);
            }

            if (updatedContext.getString(R.string.religion_no_religion).equalsIgnoreCase(text)) {
                return context.getString(R.string.religion_no_religion);
            }
        }

        return text;
    }

    public int getIndex(AutoCompleteTextView autoCompleteTextView, String target) {
        ListAdapter adapter = autoCompleteTextView.getAdapter();
        if (adapter != null) {
            for (int i = 0; i < adapter.getCount(); i++) {
                if (adapter.getItem(i).toString().equalsIgnoreCase(target)) {
                    return i; // Return the index if found
                }
            }
        }
        return -1; // Return -1 if not found
    }
    public static String getPreTermEdit(String val, String locale) {
        if (locale.equalsIgnoreCase("mr")) {
            switch (val) {
                case "Yes":
                    val = "होय";
                    break;
                case "No":
                    val = "नाही";
                    break;
                default:
                    return val;
            }
        }

        return val;
    }

    public static String getPreTerm(String val, String locale) {
        if (locale.equalsIgnoreCase("mr")) {
            switch (val) {
                case "होय":
                    val = "Yes";
                    break;
                case "नाही":
                    val = "No";
                    break;
                default:
                    return val;
            }
        }
        return val;
    }
    public static String getWaterSourceDistance(String text, Context context, String locale) {
        Context updatedContext;

        if (locale.equalsIgnoreCase("mr")) {
            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
            configuration.setLocale(new Locale("en"));
            updatedContext = context.createConfigurationContext(configuration);

            if (context.getString(R.string.meter).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.meter);
            }

            if (context.getString(R.string.km).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.km);
            }
        }

        return text;
    }

    public static String getWaterSourceDistanceEdit(String text, Context context, String locale) {
        Context updatedContext;

        if (locale.equalsIgnoreCase("mr")) {
            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
            configuration.setLocale(new Locale("en"));
            updatedContext = context.createConfigurationContext(configuration);

            if (text.contains(updatedContext.getString(R.string.meter))) {
                return context.getString(R.string.meter);
            }

            if (text.contains(updatedContext.getString(R.string.km))) {
                return context.getString(R.string.km);
            }
        }

        return text;
    }
    public static String getCultivableLand(String text, Context context, String locale) {
        if (locale.equalsIgnoreCase("mr")) {
            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
            configuration.setLocale(new Locale("en"));
            Context updatedContext = context.createConfigurationContext(configuration);

            if (context.getString(R.string.hectare).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.hectare);
            }

            if (context.getString(R.string.acre).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.acre);
            }

            if (context.getString(R.string.bigha).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.bigha);
            }

            if (context.getString(R.string.gunta).equalsIgnoreCase(text)) {
                return updatedContext.getString(R.string.gunta);
            }
        }

        return text;
    }

    public static String getCultivableLandEdit(String text, Context context, String locale) {

        if (locale.equalsIgnoreCase("mr")) {
            Configuration configuration = new Configuration(IntelehealthApplication.getAppContext().getResources().getConfiguration());
            configuration.setLocale(new Locale("en"));
            Context updatedContext = context.createConfigurationContext(configuration);

            if (text.contains(updatedContext.getText(R.string.hectare))) {
                return context.getString(R.string.hectare);
            }

            if (text.contains(updatedContext.getText(R.string.acre))) {
                return context.getString(R.string.acre);
            }

            if (text.contains(updatedContext.getText(R.string.bigha))) {
                return context.getString(R.string.bigha);
            }

            if (text.contains(updatedContext.getText(R.string.gunta))) {
                return context.getString(R.string.gunta);
            }
        }

        return text;
    }
    public static boolean validateFields(List<View> mandatoryFields) {
        boolean result = true;
        for (View mandatoryField : mandatoryFields) {
            if (mandatoryField instanceof EditText) {
                if (TextUtils.isEmpty(((EditText) mandatoryField).getText())) {
                    result = false;
                }
            } else if (mandatoryField instanceof RadioGroup) {
                result = ((RadioGroup) mandatoryField).getCheckedRadioButtonId() != -1;
            } else if (mandatoryField instanceof Spinner) {
                result = ((Spinner) mandatoryField).getSelectedItemPosition() != 0;
            }
        }
        return result;
    }

}