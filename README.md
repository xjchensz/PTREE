Package xjc.covertree consists codes for cover tree
To build a cover tree, refer to xjc.covertree.CoverTreeTester



xjc.data.PTree.PurTree.PurchaseTree is the code for PurchaseTree

To use PurTreeClust, follow like this:
1) Build PurchaseTree from data, see xjc.PTree.PurTree.build.BuildSuperStoreData
2) Build cover tree, see xjc.PTree.PurTree.BuildCoverTreeDataset
3) Clustering, see xjc.PTree.PurTree.BuildCluster

Note:
1) ProductTree can be stored as xml file, see xjc.data.PTree.toXML()
2) PurTree can be stored as binary file and xml file, see xjc.data.PTree.PurTree.save(File dir) and xjc.data.PTree.PurTree.toXML()
3) cover tree can be stored, see xjc.data.PTree.PurTree.PurTreeClust.CoverTreeSetSemanticDataset.write(DataOutput out)
