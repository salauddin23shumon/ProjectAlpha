package com.sss.bitm.projectalpha.tourEvent.eventAdapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.sss.bitm.projectalpha.R;
import com.sss.bitm.projectalpha.tourEvent.eventDataModel.Expense;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ViewHolder> {

    private List<Expense>expenseList;
    private Context context;
    private Fragment fragment;

    private ExpenseItemAction itemAction;

    public ExpenseAdapter(List<Expense> expenseList, Context context, Fragment fragment) {
        this.expenseList = expenseList;
        this.context = context;
        itemAction= (ExpenseItemAction) fragment;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(context).inflate(R.layout.expense_single_row,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Expense expense=expenseList.get(position);
        holder.expName.setText(expense.getExpenseName());
        holder.expCost.setText(String.valueOf(expense.getExpenseCost()));

        holder.menuDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.expense_delete_menu, popupMenu.getMenu());
                popupMenu.show();
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {

                        switch (menuItem.getItemId()) {
                            case R.id.expense_delete:
                                itemAction.onExpenseDelete(expense);
                                break;
                        }
                        return false;
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        Log.d("frm adapter", "getItemCount: "+expenseList.size());
        return expenseList.size();
    }

    public void updateList(List<Expense> expenseList) {
        this.expenseList=expenseList;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView expName, expCost, menuDot;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            expName=itemView.findViewById(R.id.exp_nameTV);
            expCost=itemView.findViewById(R.id.exp_amountTV);
            menuDot=itemView.findViewById(R.id.exp_action);
        }
    }

    public interface ExpenseItemAction{
        void onExpenseDelete(Expense expense);
    }
}
