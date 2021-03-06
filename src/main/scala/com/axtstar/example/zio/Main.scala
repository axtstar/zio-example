package com.axtstar.example.zio

import java.io.IOException

import zio._
import zio.console._

object Main extends App {

  def run(args: List[String]): URIO[Console, Int] =
    //failure as 1, success as 0
    args match {
      case x::_ =>
        x match {
          case "howOldAreYou" =>
            //sbt "run 0"
            howOldAreYou.fold(_ => 1, _ => 0)
          case "basicOperation" =>
            //I'm not sure how to handle can
            basicOperation.fold(_ => 1, _ => 0)
          case "basicFailure" =>
            basicFailure.fold(_ => 1, _ => 0)
          case "mapping" => {
            val result = for {
              right <- mapping
              _ <- putStrLn(s"mapping: ${right}")
            } yield()

           result.map(_ => 0)
          }
          case "zipped" => {
            val zipped: UIO[(String, Int)] =
              ZIO.succeed("4").zip(ZIO.succeed(2))

            val result = for {
              target <- zipped
              _ <- putStrLn(s"${target._1}  & ${target._2}" )
            } yield ()

            result.map(x =>   0)
          }
          case "either" => {
            val zeither: UIO[Either[String, Int]] =
              IO.fail("Uh oh!").either

            val result = for {
              target <- zeither
              _ <- putStrLn(s"isLeft : ${target.isLeft} : ${target.left.getOrElse("")}")
            } yield()

            result.map(x =>   0)
          }

          case "sqrt" => {
            val result = for {
              target <- sqrt(UIO.succeed(50))
              _ <- putStrLn(s"sqrt: ${target}")
            } yield ()

            result.fold(_ =>   1, _=> 0)
          }

        }
      case _ =>
        //sbt run
        helloWorld.fold(_ => 1, _ => 0)

    }
  val helloWorld: ZIO[Console, IOException, Unit] =
    for {
      _ <- putStrLn("Hello! What is your name?")
      name <- getStrLn
      _ <- putStrLn(s"Hello, $name, welcome to ZIO!")

    } yield ()

  val howOldAreYou: ZIO[Console, IOException, Unit] =
    for {
      _ <- putStrLn("How old are you?")
      age <- getStrLn
      //age is non int, the below method fails
      _ <- putStrLn(s"You are ${age.toInt} years old!")

    } yield ()

  val basicOperation: ZIO[Console, IOException, Unit] = {
    //this is effect, eager evaluation
    val s1 = ZIO.succeed(42).map(_ * 2)
    // type alisa of the above
    val s2 = Task.succeed(42).map(_ * 2)
    for {
      target <- s1
      _ <- putStrLn(s"result is $target")
      target <- s2
      _ <- putStrLn(s"result is $target")
    } yield ()
  }

  val basicFailure: ZIO[Console, Serializable, Unit] = {
    val f1 = ZIO.fail("OOps")
    val f2 = Task.fail(new Exception("OOps"))
    for {
      target <- f1
      _ <- putStr(s"f1:${target}")
      target <- f2
      _ <- putStr(s"f2:${target}")
    } yield ()
  }

  val mapping: UIO[Int] = {
    IO.succeed(21).map(_ * 2)
  }

  val fromOption = {
    val zoption: ZIO[Any, Unit, Int] = ZIO.fromOption(Some(2))
    val zoption2: ZIO[Any, String, Int] = zoption.mapError(_ => "It wasn't there!")
  }

  val fromEither = {
    val zeither:IO[Nothing, String] = ZIO.fromEither(Right("Success!"))
    val zeither2:IO[String, Nothing] = ZIO.fromEither(Left("Success!"))
  }

  val fromTry = {
    import scala.util.Try
    val ztry:Task[Int] = ZIO.fromTry(Try(2 / 0))
  }

  val fromFunction = {
    val zfun: ZIO[Long, Nothing, Int] = ZIO.fromFunction((i: Long) => (i * i).toInt)
  }

  val fromFuture = {
    import scala.concurrent.Future

    lazy val future = Future.successful("Hello!")

    val zfuture: Task[String] =
      ZIO.fromFuture { implicit ec =>
        future.map(_ => "Goodbye!")
      }
  }

  val syncSideEffect = {
    import scala.io.StdIn

    val getStrLn: Task[String] =
      ZIO.effect(StdIn.readLine())
  }

  val syncSideEffect2 = {
    def putStrLn(line: String): UIO[Unit] =
      ZIO.effectTotal(println(line))
  }

  def sqrt(io: UIO[Double]): IO[String, Double] =
    ZIO.absolve(
      io.map(value =>
        if (value < 0.0) Left("Value must be >= 0.0")
        else Right(Math.sqrt(value))
      )
    )


}