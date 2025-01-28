package org.intelehealth.app.activities.prescription.thermalprinter

class BaseEnum {  companion object {
    const val NONE = -1
    const val CMD_ESC = 1
    const val CMD_TSC = 2
    const val CMD_CPCL = 3
    const val CMD_ZPL = 4
    const val CMD_PIN = 5

    const val CON_BLUETOOTH = 1
    const val CON_BLUETOOTH_BLE = 2
    const val CON_WIFI = 3
    const val CON_USB = 4
    const val CON_COM = 5

    const val NO_DEVICE = -1
    const val HAS_DEVICE = 1
}

    @Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.SOURCE)
    annotation class CmdType

    @Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ConnectType

    @Target(AnnotationTarget.TYPE, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.SOURCE)
    annotation class ChooseDevice
}