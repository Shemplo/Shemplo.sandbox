import numpy  as np
import pandas as pd
import sys

from sklearn.feature_selection import chi2, f_classif, mutual_info_classif
from sklearn.ensemble  import RandomForestClassifier, ExtraTreesClassifier
from sklearn.feature_selection import SelectKBest, SelectFromModel, RFECV
from sklearn.model_selection import train_test_split, RandomizedSearchCV
from sklearn.metrics import accuracy_score, make_scorer, f1_score
from sklearn.svm import SVC, LinearSVC

seed_shift = int   (sys.argv [1])
estimators = int   (sys.argv [2])
test_size  = float (sys.argv [3])

train_input = pd.read_csv ("../../../temp/dataset.csv", index_col = 0, sep = ";")

feature_names = train_input.columns.values
features_size = len (train_input.columns)

train_data    = train_input.values [:, :features_size - 1]
train_target  = train_input.values [:, -1]

train_data = pd.DataFrame (train_data)
train_target = pd.Series (train_target)

np.random.seed (163 + seed_shift)
train_data, test_data, train_target, test_target =\
	train_test_split (train_data, train_target, 
					  test_size = test_size)
	
forest = RandomForestClassifier (n_estimators=estimators,
                                 random_state=0)
forest.fit (train_data, train_target)

scorer = make_scorer (f1_score)
scores = scorer (forest, test_data, test_target)
print "Match score: %f" % scores 

importances = forest.feature_importances_
std = np.std ([tree.feature_importances_ for tree in forest.estimators_],
              axis=0)
indices = np.argsort (importances) [::-1]							  

output = open ("../../../temp/sklearn-%s.txt" % str (estimators), 'w')
output.write (str (scores) + "\n")
for f in range(train_data.shape[1]):
	output.write ("%s %f\n" % (feature_names [indices [f]], importances [indices [f]]))
output.close ()

