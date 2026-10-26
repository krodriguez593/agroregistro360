package ec.dx3os.core

import ec.dx3os.core.projection.AndroidAutoAvailability
import ec.dx3os.core.projection.AndroidAutoEvaluator
import ec.dx3os.core.projection.AndroidAutoInputs
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AndroidAutoEvaluatorTest {
    @Test fun defaultIsUnverifiedAndNotLaunchable() {
        val p = AndroidAutoEvaluator.present(AndroidAutoInputs())
        assertFalse(p.canLaunchReceiver)
        assertTrue(p.title.contains("sin verificar"))
    }

    @Test fun receiverPackageAloneIsNotEnough() {
        // Una app instalada no convierte la radio en receptor certificado.
        val p = AndroidAutoEvaluator.present(AndroidAutoInputs(AndroidAutoAvailability.UNVERIFIED, receiverPackageInstalled = true))
        assertFalse(p.canLaunchReceiver)
    }

    @Test fun certifiedWithReceiverCanLaunch() {
        assertTrue(AndroidAutoEvaluator.present(AndroidAutoInputs(AndroidAutoAvailability.CERTIFIED_RECEIVER_PRESENT, true)).canLaunchReceiver)
        assertFalse(AndroidAutoEvaluator.present(AndroidAutoInputs(AndroidAutoAvailability.CERTIFIED_RECEIVER_PRESENT, false)).canLaunchReceiver)
        assertFalse(AndroidAutoEvaluator.present(AndroidAutoInputs(AndroidAutoAvailability.NOT_SUPPORTED, true)).canLaunchReceiver)
    }
}
