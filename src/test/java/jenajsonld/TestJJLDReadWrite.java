/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jenajsonld;

import com.hp.hpl.jena.query.Dataset;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileUtils;
import junit.framework.Assert;
import org.junit.Test;

import java.io.*;

/** tests : JSONLD->RDF ; JSONLD->RDF->JSONLD */
public class TestJJLDReadWrite
{
    private static String DIR = "testing/RIOT/jsonld/" ; 

    @Test public void read_g01() throws FileNotFoundException
    { graphJ2R("graph1.jsonld", "graph1.ttl") ; }

//    @Test public void read_ds01() { datasetJ2R("graph1.jsonld", "graph1.ttl") ; }
//
//    @Test public void read_ds02() { datasetJ2R("dataset1.jsonld", "dataset1.trig") ; }


    private void graphJ2R(String inFile, String outFile) throws FileNotFoundException
    {
        inFile = DIR+inFile ;
        outFile = DIR+outFile ;
        JsonLDReader reader = new JsonLDReader();
        Model model1 = reader.read(new FileInputStream(inFile), "").getDefaultModel();
        Model model2 = ModelFactory.createDefaultModel();
        model2.read(new FileReader(outFile), "", FileUtils.guessLang(outFile, FileUtils.langTurtle));
        Assert.assertTrue(model1.isIsomorphicWith(model2)) ;
    }

//    private void datasetJ2R(String inFile, String outFile)
//    {
//        inFile = DIR+inFile ;
//        outFile = DIR+outFile ;
//        Dataset ds1 = RDFDataMgr.loadDataset(inFile) ;
//        Dataset ds2 = RDFDataMgr.loadDataset(outFile) ;
//    }


    @Test public void roundtrip_01() throws IOException
    { rtRJRg("graph1.ttl") ; }

//    @Test public void roundtrip_02() { rtRJRds("graph1.ttl") ; }
//
//    @Test public void roundtrip_03() { rtRJRds("dataset1.trig") ; }
//
    static void rtRJRg(String filename) throws IOException
    {
        filename = DIR+filename ;
        // Read in
        Model model = ModelFactory.createDefaultModel();
        model.read(new FileReader(filename), "", FileUtils.guessLang(filename, FileUtils.langTurtle));
        JsonLDReader reader = new JsonLDReader();
        JsonLDWriter writer = new JsonLDWriter(false);

        // Write a JSON-LD
        StringWriter out = new StringWriter();
        writer.write(out, model.getGraph(), "http://example/") ;
        String jsonld = out.toString();
        System.out.println("intermediate json-ld:" + jsonld);
        ByteArrayInputStream in = new ByteArrayInputStream(jsonld.getBytes("UTF-8"));
        // Read as JSON-LD
        Dataset result = reader.read(in,"http://example/");
        Model model2 = result.getDefaultModel();

        System.out.println("model1:");
        model.write(System.out,FileUtils.langTurtle,"http://example/");
        System.out.println("model2:");
        model2.write(System.out, FileUtils.langTurtle,"http://example/");
        System.out.println("jsonld:");
        writer.write(System.out, model.getGraph(),"http://example/");
        // Compare
        Assert.assertTrue(model.isIsomorphicWith(model2));
    }
//
//    static void rtRJRds(String filename)
//    {
//        filename = DIR+filename ;
//        Dataset ds1 = RDFDataMgr.loadDataset(filename) ;
//
//        // Write a JSON-LD
//        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
//        RDFDataMgr.write(out, ds1, JSONLD) ;
//        ByteArrayInputStream r = new ByteArrayInputStream(out.toByteArray()) ;
//
//        // Read as JSON-LD
//        Dataset ds2 = DatasetFactory.createMem() ;
//        RDFDataMgr.read(ds2, r, null, JSONLD) ;
//
//        if ( ! isIsomorphic(ds1, ds2) )
//        {
//            SSE.write(ds1) ;
//            SSE.write(ds2) ;
//        }
//
//        assertTrue(isIsomorphic(ds1, ds2) ) ;
//    }
//
//    private static boolean isIsomorphic(Dataset ds1, Dataset ds2)
//    {
//        return DatasetLib.isomorphic(ds1, ds2) ;
//    }
}


