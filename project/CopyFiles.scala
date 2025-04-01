import play.twirl.sbt.Import.TwirlKeys
import sbt.*
import sbt.Keys.*

object CopySources {
  /** Copies source files from one module to another, and applies transformations
   * with support for Scala 2 and Scala 3 specific directories */
  def copySources(module: Project, transformSource: String => String, transformResource: String => String) = {
    def transformWith(fromSettingBase: SettingKey[File], toSetting: SettingKey[File], transform: String => String) =
      Def.task {
        val fromBase = (module / fromSettingBase).value
        val to = toSetting.value
        val currentScalaVersion = scalaVersion.value
        val isScala3 = currentScalaVersion.startsWith("3.")

        // Define base directory and potential version-specific directories
        val baseDir = fromBase
        val scala2Dir = new File(fromBase.getParentFile, s"${fromBase.getName}-2")
        val scala3Dir = new File(fromBase.getParentFile, s"${fromBase.getName}-3")

        // Collect all relevant directories based on Scala version
        val dirsToProcess = Seq(
          baseDir -> to,
          (if (isScala3 && scala3Dir.exists) scala3Dir -> to else null),
          (if (!isScala3 && scala2Dir.exists) scala2Dir -> to else null)
        ).filterNot(_ == null)

        // Process all directories
        val processedFiles = dirsToProcess.flatMap { case (from, target) =>
          val files = (from ** "*").get.filterNot(_.isDirectory)
          println(s"Copying and transforming the following files for ${moduleName.value} scalaVersion $currentScalaVersion from ${from.getPath}:\n${files.map("  " + _).mkString("\n")}")

          files.map { file =>
            val relativePath = file.getPath.stripPrefix(from.getPath)
            val targetFile = new java.io.File(target.getPath + relativePath)
            IO.createDirectory(targetFile.getParentFile)
            IO.write(targetFile, transform(IO.read(file)))
            targetFile
          }
        }

        processedFiles
      }

    def include(location: File) = {
      val files = (location ** "*").get.filterNot(_.isDirectory)
      files.map(file => file -> file.getPath.stripPrefix(location.getPath))
    }

    Seq(
      Compile / sourceGenerators   += transformWith(Compile / scalaSource      , Compile / sourceManaged  , transformSource  ).taskValue,
      Compile / resourceGenerators += transformWith(Compile / resourceDirectory, Compile / resourceManaged, transformResource).taskValue,
      Test    / sourceGenerators   += transformWith(Test    / scalaSource      , Test    / sourceManaged  , transformSource  ).taskValue,
      Test    / resourceGenerators += transformWith(Test    / resourceDirectory, Test    / resourceManaged, transformResource).taskValue,
      Compile / TwirlKeys.compileTemplates / sourceDirectories += (module / baseDirectory).value / s"src/main/twirl",
      // generated sources are not included in source.jar by default
      Compile / packageSrc / mappings ++= include((Compile / sourceManaged).value) ++
        include((Compile / resourceManaged).value)
    )
  }
}
