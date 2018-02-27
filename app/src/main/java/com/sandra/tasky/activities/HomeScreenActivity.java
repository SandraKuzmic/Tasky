package com.sandra.tasky.activities;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.DataSetObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.TaskyUtils;
import com.sandra.tasky.adapter.HomeListAdapter;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;
import com.sandra.tasky.entity.TaskCategory;

import net.danlew.android.joda.JodaTimeAndroid;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class HomeScreenActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final int REQUEST_CODE = 1;

    private TaskDatabase database;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;

    private FloatingActionButton fabAddTask;

    private List<SimpleTask> tasks;
    private List<TaskCategory> categories;
    private Map<Long, Integer> categoriesCount;

    private int selectedCategoryId = (int) TaskyConstants.ALL_CATEGORY_ID;

    private Toast mToast;

    private TasksDataObserver observer;
    private HomeListAdapter homeListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen_drawer);

        JodaTimeAndroid.init(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open_nav, R.string.close_nav);

        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        fabAddTask = (FloatingActionButton) findViewById(R.id.fab_add_task);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewTask();
            }
        });

        //open db
        new getDataAsyncTask().execute();

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        handleIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new getDataAsyncTask().execute();
    }

    @Override
    protected void onPause() {
        TaskyUtils.updateWidget(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        TaskyUtils.updateWidget(this);
        super.onDestroy();
        homeListAdapter.unregisterDataSetObserver(observer);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.home_screen_menu, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.home_menu_search));

        SearchManager manager = (SearchManager) getSystemService(SEARCH_SERVICE);
        ComponentName componentName = getComponentName();

        searchView.setSearchableInfo(manager.getSearchableInfo(componentName));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                search(newText);
                return true;
            }
        });

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.home_menu_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                hideItems(menu);
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                showItems(menu);
                return true;
            }
        });

        return true;
    }

    private void hideItems(Menu menu) {
        menu.setGroupVisible(R.id.menu_group_hidden, false);
        fabAddTask.setVisibility(View.GONE);
    }

    private void showItems(Menu menu) {
        menu.setGroupVisible(R.id.menu_group_hidden, true);
        fabAddTask.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.home_menu_search:
                return true;
            case R.id.home_menu_sort:
                sortBy();
                return true;
            case R.id.home_menu_delete_all:
                deleteTasks();
                return true;
            default:
                mToast = TaskyUtils.addToast(mToast, this, getString(R.string.error), true);
                return super.onOptionsItemSelected(item);
        }
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            search(query);
        }
    }

    private void search(String query) {
        updateListView(query);
    }

    private void sortBy() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.sort_by);

        final SharedPreferences preferences = getSharedPreferences(TaskyConstants.PREF_GENERAL, MODE_PRIVATE);
        final int selectedSortOption = preferences.getInt(TaskyConstants.PREF_SORT, TaskyConstants.SORT_DEFAULT);

        String[] sortOptions = {getString(R.string.due_date), getString(R.string.title), getString(R.string.completed)};

        builder.setSingleChoiceItems(sortOptions, selectedSortOption, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                preferences.edit()
                        .putInt(TaskyConstants.PREF_SORT, which)
                        .apply();

                updateListView(null);

                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void deleteTasks() {
        AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);
        builder.setTitle(getString(R.string.delete));

        final List<SimpleTask> filteredTasks = sortAndFilterTasks(null);

        if (filteredTasks.size() == 0) {
            builder.setMessage(R.string.nothing_to_delete);

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
        } else {

            builder.setMessage(getResources().getQuantityString(R.plurals.delete_alert, filteredTasks.size(), filteredTasks.size()));

            builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (selectedCategoryId == TaskyConstants.ALL_CATEGORY_ID) {
                        database.deleteAllTasks();
                    } else {
                        long[] ids = new long[filteredTasks.size()];
                        for (int i = 0; i < filteredTasks.size(); i++) {
                            ids[i] = filteredTasks.get(i).getId();
                        }
                        database.deleteAllTasksInCategory(ids);
                    }
                    new getDataAsyncTask().execute();
                }
            });

            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        }

        builder.show();
    }


    private void createNewTask() {
        Intent newTaskIntent = new Intent(this, TaskActivity.class);
        newTaskIntent.putExtra(TaskyConstants.SELECTED_CATEGORY_KEY, selectedCategoryId);
        startActivityForResult(newTaskIntent, REQUEST_CODE);
    }

    private void updateListView(String query) {
        final List<SimpleTask> list = sortAndFilterTasks(query);

        observer = new TasksDataObserver();

        homeListAdapter = new HomeListAdapter(HomeScreenActivity.this, list);
        homeListAdapter.registerDataSetObserver(observer);

        ListView listView = (ListView) findViewById(R.id.home_list);
        listView.setAdapter(homeListAdapter);

        listView.setEmptyView(findViewById(R.id.home_empty_view));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent openTaskIntent = new Intent(HomeScreenActivity.this, TaskActivity.class);
                openTaskIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, list.get(position));
                openTaskIntent.putExtra(TaskyConstants.SELECTED_CATEGORY_KEY, selectedCategoryId);
                startActivityForResult(openTaskIntent, REQUEST_CODE);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                final AlertDialog.Builder builder = new AlertDialog.Builder(HomeScreenActivity.this);
                builder.setTitle(list.get(position).getTitle());

                ArrayAdapter<String> optionList = new ArrayAdapter<>(HomeScreenActivity.this, android.R.layout.simple_list_item_1);
                optionList.add(getString(R.string.delete));

                builder.setAdapter(optionList, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mToast = TaskyUtils.addToast(mToast, HomeScreenActivity.this, R.string.task_deleted, true);
                        database.deleteTasks(list.get(position));
                        new getDataAsyncTask().execute();
                        dialog.cancel();
                    }
                });

                builder.show();
                return true;
            }
        });

        TaskyUtils.updateWidget(this);
    }

    private void setActionBar(CharSequence title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                new getDataAsyncTask().execute();
            }
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_manage:
                startActivityForResult(new Intent(HomeScreenActivity.this, CategoriesActivity.class), REQUEST_CODE);
                break;
            case R.id.nav_settings:
                startActivity(new Intent(HomeScreenActivity.this, SettingsActivity.class));
                break;
            default:
                openCategory(item.getItemId(), item.getTitle());
                break;
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openCategory(int categoryId, CharSequence categoryTitle) {
        setActionBar(categoryTitle);

        selectedCategoryId = categoryId;

        updateListView(null);
    }

    private void updateCategoriesList() {

        navigationView.getMenu().removeGroup(R.id.menu_group_top);
        navigationView.getMenu().add(R.id.menu_group_top, (int) TaskyConstants.ALL_CATEGORY_ID, 1,
                (getString(R.string.all_num, tasks.size())));

        int categorySize = 0;
        for (TaskCategory category : categories) {
            navigationView.getMenu().add(R.id.menu_group_top, (int) category.getId().longValue(), 2,
                    category.getTitle() + " (" + categoriesCount.get(category.getId()) + ")");
            categorySize += categoriesCount.get(category.getId());
        }

        //add others if there is at least one category
        if (categories.size() != 0) {
            navigationView.getMenu().add(R.id.menu_group_top, (int) TaskyConstants.OTHERS_CATEGORY_ID,
                    categories.size() + 1, getString(R.string.others_num, tasks.size() - categorySize));
        }
    }

    private List<SimpleTask> sortAndFilterTasks(String query) {
        List<SimpleTask> filteredTasks;

        //filter by category
        if (selectedCategoryId == TaskyConstants.ALL_CATEGORY_ID) {
            filteredTasks = new LinkedList<>(tasks);
        } else {
            filteredTasks = new LinkedList<>();
            for (SimpleTask task : tasks) {
                //get others or selected category
                if ((task.getCategory() == null && selectedCategoryId == TaskyConstants.OTHERS_CATEGORY_ID)
                        || (task.getCategory() != null && task.getCategory().getId().equals((long) selectedCategoryId))) {
                    filteredTasks.add(task);
                }
            }
        }

        List<SimpleTask> queryTasks;

        if (query != null) {
            queryTasks = new LinkedList<>();
            for (SimpleTask task : filteredTasks) {
                if (task.getTitle().toLowerCase().contains(query.toLowerCase().trim())) {
                    queryTasks.add(task);
                }
            }
        } else {
            queryTasks = new LinkedList<>(filteredTasks);
        }

        //sort
        Collections.sort(queryTasks, new Comparator<SimpleTask>() {
            @Override
            public int compare(SimpleTask o1, SimpleTask o2) {
                switch (getSharedPreferences(TaskyConstants.PREF_GENERAL, MODE_PRIVATE).getInt(TaskyConstants.PREF_SORT, TaskyConstants.SORT_DEFAULT)) {
                    case TaskyConstants.SORT_DUE_DATE:
                        return 0;
                    case TaskyConstants.SORT_TITLE:
                        return o1.getTitle().toLowerCase().compareTo(o2.getTitle().toLowerCase());
                    case TaskyConstants.SORT_COMPLETED:
                        return o1.isCompleted() ? 1 : -1;
                    default:
                        return 0;

                }
            }
        });

        return queryTasks;
    }

    private class getDataAsyncTask extends AsyncTask<String, Integer, Integer> {
        @Override
        protected Integer doInBackground(String... params) {
            database = new TaskDatabase(HomeScreenActivity.this);

            tasks = database.getAllTasks();
            categoriesCount = database.getCategoriesTaskCount();
            categories = database.getAllCategories();

            return null;
        }

        @Override
        protected void onPostExecute(Integer retValue) {
            updateCategoriesList();
            updateListView(null);

            setActionBar(navigationView.getMenu().findItem(selectedCategoryId).getTitle());
        }
    }

    private class TasksDataObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            super.onChanged();
            updateListView(null);
        }
    }

}
