/*
 *  Copyright (C) 2011-2013 The Animo Project
 *  http://animotron.org
 *
 *  This file is part of Animotron.
 *
 *  Animotron is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  Animotron is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of
 *  the GNU Affero General Public License along with Animotron.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package snow;

import java.lang.reflect.Field;

import static mneme.ID.fromString;

//import org.neo4j.graphdb.Node;
//import org.neo4j.graphdb.Transaction;
//import static mneme.Core.DB;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class Primitives {

    // Languages
    public static final ID ORIGINAL_LANGUAGE = fromString("1d4b9e8e-f567-4196-8438-b5e4956d06a9");

    public static final ID LANG = fromString("ab888b29-5626-4a88-9b22-627ff643aa05");

    public static final ID ENG = fromString("7da3adbc-1ae0-404a-b7fb-b1dcd7d69285");
    public static final ID RUS = fromString("7fcaa8cb-1568-4f0d-a835-081408492424");

    public static final ID IDENTIFIER = fromString("d34195bd-a8e1-4b19-933a-f84b1c090b78");

    public static final ID TYPE = fromString("75d47763-ba75-4172-8e3a-f99a5b2405a0");
    public static final ID NAME = fromString("0d68e999-179b-419f-8ffd-5001ba47a5f0");
    public static final ID LINK = fromString("7fbef892-f657-4533-bef3-301172c4c9d1");

    public static final ID PRIMITIVE = fromString("06424618-c8bc-45e3-b28f-23182256426a");

    // Statuses
    public static final ID PROPOSAL = fromString("be756e36-1382-4431-b678-e0011ba3d6fe");
    public static final ID TOCHECK = fromString("f130c498-9801-4c22-9a3b-7644d943cafc");

    public static final ID UNKNOWN = fromString("3b49adeb-ad2e-4f47-925f-1e945f5d8482");

    public static final ID LABEL_LANG = fromString("87c27823-50de-4dcd-9105-911cd49e235a");
    public static final ID LABEL_VISUAL = fromString("27f89d7f-83da-47b5-8117-cbfecf6cdac4");

    public static final ID VISUAL = fromString("4e5bb1c0-5b28-4c9f-b8a8-74e6dcadb204");

    //
    public static final ID PERSON = fromString("2a4ffe33-2d68-4e06-bcf1-a01f54f77655");

    public static final ID FILM = fromString("191b68bb-5b06-42bf-a7f3-c1b546457145");

    public static void init() {

        for (Field field : Primitives.class.getDeclaredFields()) {
            try {

//                try (Transaction txn = Core.DB.beginTx()) {
//                    ID id = (ID) field.get(Primitives.class);
//                    try {
//                        TermInDB.term(id);
//                    } catch (IllegalArgumentException e) {
//                        String chars = field.getName().toLowerCase().replace('_', ' ');
//                        createTerm(id._, chars);
//                    }
//
//                    txn.success();
//                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void createTerm(String id, String chars) {
        // System.out.println(chars);

//        Node node = DB.createNode();
//        node.setProperty(IDENTIFIER._, id);
//        node.setProperty(TYPE._, PRIMITIVE._);
//
//        TermInDB term = TermInDB.term(node);
//        term.label(ORIGINAL_LANGUAGE, chars);
    }

//    public static String label(ID id) {
//        if (id == null) return "?";
//
//        try {
//            TermInDB term = TermInDB.term(id);
//
//            return term.label();
//
//        } catch (IllegalArgumentException e) {
//            return id._;
//        }
//    }
//
//    public static String label(String id) {
//        if (id == null) return "?";
//
//        try {
//            TermInDB term = TermInDB.term(ID.fromString(id));
//
//            return term.label();
//
//        } catch (IllegalArgumentException e) {
//            return id;
//        }
//    }
}
