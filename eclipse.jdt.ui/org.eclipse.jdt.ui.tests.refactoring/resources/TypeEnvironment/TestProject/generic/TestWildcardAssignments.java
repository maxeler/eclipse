package generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestWildcardAssignments {
	Object o= null;
	
	List<String> list_string;
	List<Number> list_number;
	List<Integer> list_integer;
	
	List<?> list_wildcard;
	List<? extends Object> list_lower_object;
	List<? extends Number> list_upper_number;
	List<? extends Integer> list_upper_integer;
	List<? super Number> list_lower_number;
	List<? super Integer> list_lower_integer;
	
	List<List<? extends String>> list_list_upper_string;
	List<ArrayList<String>> list_arraylist_string;
	
	List<List<? extends Object>> list_list_upper_object;
	List<List<String>> list_list_string;
	List<List> list_raw_list;
	
	Collection<?> coll_wildcard;
	Collection<? extends Collection<? extends Number>> coll_upper_coll_upper_number;
	Collection<? extends Collection<? extends Integer>> coll_upper_coll_upper_integer;
	
	Collection<? super Collection<? super Number>> coll_lower_coll_lower_number;
	Collection<? super Collection<? super Integer>> coll_lower_coll_lower_integer;
	
	Collection<? extends Collection<? super Number>> coll_upper_coll_lower_number;
	Collection<? extends Collection<? super Integer>> coll_upper_coll_lower_integer;		

	Collection<? super Collection<? extends Number>> coll_lower_coll_upper_number;
	Collection<? super Collection<? extends Integer>> coll_lower_coll_upper_integer;
	
	List<? extends Object[]> list_upper_object_array;
	List<? extends Number[]> list_upper_number_array;
	List<? super Object[]> list_lower_object_array;
	List<? super Number[]> list_lower_number_array;
	List<Number[]> list_number_array;
	List<Integer[]> list_integer_array;
	
	// repeated (check different wildcards):
	List<?> list_wildcard2;
	List<? extends Object> list_lower_object2;
	List<? extends Number> list_upper_number2;
	List<? extends Integer> list_upper_integer2;
	List<? super Number> list_lower_number2;
	List<? super Integer> list_lower_integer2;
	
	List<List<? extends String>> list_list_upper_string2;
	List<ArrayList<String>> list_arraylist_string2;
	
	List<List<? extends Object>> list_list_upper_object2;
	List<List<String>> list_list_string2;
	List<List> list_raw_list2;
	
	Collection<?> coll_wildcard2;
	Collection<? extends Collection<? extends Number>> coll_upper_coll_upper_number2;
	Collection<? extends Collection<? extends Integer>> coll_upper_coll_upper_integer2;
	
	Collection<? super Collection<? super Number>> coll_lower_coll_lower_number2;
	Collection<? super Collection<? super Integer>> coll_lower_coll_lower_integer2;
	
	Collection<? extends Collection<? super Number>> coll_upper_coll_lower_number2;
	Collection<? extends Collection<? super Integer>> coll_upper_coll_lower_integer2;		

	Collection<? super Collection<? extends Number>> coll_lower_coll_upper_number2;
	Collection<? super Collection<? extends Integer>> coll_lower_coll_upper_integer2;
	
	List<? extends Object[]> list_upper_object_array2;
	List<? extends Number[]> list_upper_number_array2;
	List<? super Object[]> list_lower_object_array2;
	List<? super Number[]> list_lower_number_array2;
	List<Number[]> list_number_array2;
	List<Integer[]> list_integer_array2;
	
}
