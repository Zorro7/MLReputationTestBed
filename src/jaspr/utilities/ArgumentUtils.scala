package jaspr.utilities

import jaspr.weka.core.LearnerFactory

import scala.util.Try

/**
  * Created by phil on 31/03/16.
  */
object ArgumentUtils {

  def convargs(sargs: Seq[String]): Seq[AnyRef] = {
    for (arg <- sargs) yield
      Try(arg.toBoolean.asInstanceOf[java.lang.Boolean]).getOrElse(
        Try(arg.toInt.asInstanceOf[java.lang.Integer]).getOrElse(
          Try(arg.toDouble.asInstanceOf[java.lang.Double]).getOrElse(
            Try(arg.toFloat.asInstanceOf[java.lang.Float]).getOrElse(
              Try(arg.toLong.asInstanceOf[java.lang.Long]).getOrElse(
                Try(arg.toShort.asInstanceOf[java.lang.Short]).getOrElse(
                  Try(arg.toByte.asInstanceOf[java.lang.Byte]).getOrElse(
                    Try(LearnerFactory.makeLearner(arg.split("_"), false)).getOrElse(
                      Try(Class.forName(arg)).getOrElse(arg)))))))))
  }
}
