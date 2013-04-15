/*
 Copyright 2012-2013 University of Stavanger, Norway

 Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package no.uis.nio.commons;


import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;

/**
 * This class is a mock up for the PDF component's CatalogCreator.
 */
public class CatalogCreatorMock {
  
  private boolean purgeOutputDir;
  
  public void setPurgeOutputDir(boolean purgeOutputDir) {
    this.purgeOutputDir = purgeOutputDir;
  }

  private boolean isPurgeOutputDir() {
    return purgeOutputDir;
  }
  
  public void createCatalog(Path baseDir) throws Exception {
    cleanOutputDir(baseDir);
    String baseFileName = "test";
    Path filePath = baseDir.resolve(baseFileName + ".txt"); //$NON-NLS-1$
    OutputStream out = Files.newOutputStream(filePath);
    
    byte[] content = "Test content".getBytes();
    out.write(content);
    
    out.close();
  }
  
  protected void cleanOutputDir(Path outDir) throws IOException {
    if (this.isPurgeOutputDir()) {
      if (Files.isDirectory(outDir) && Files.exists(outDir)) {
        Files.walkFileTree(outDir, new SimpleFileVisitor<Path>() {

          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            Files.delete(Objects.requireNonNull(file));
            return FileVisitResult.CONTINUE;
          }

        });
      }
    }
    Files.createDirectories(outDir);
  }
}
