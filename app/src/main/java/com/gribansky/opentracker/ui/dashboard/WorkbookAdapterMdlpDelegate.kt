package com.gribansky.opentracker.ui.dashboard

import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.gribansky.opentracker.core.PositionData
import com.hannesdorfmann.adapterdelegates4.AdapterDelegate




class HistoryDelegate(

) : AdapterDelegate<MutableList<Any>>() {

    private var isFocusNeed: Boolean = false
    private var dataSize: Int = -1

    private var titleTextColor: Int = 0
    private var rawForFocus = -1

    //новые SKU которые отправлены на запись в БД но еще в процессе
    private val skuForDb = HashSet<Long>()

    private val expandPositions = mutableSetOf<Int>()

    //SKU которые уже записаны в базу
    private val skuInDb = HashSet<Long>()

    override fun isForViewType(items: MutableList<Any>, position: Int) =
        items[position] is PositionData

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {

        titleTextColor =
            ContextCompat.getColor(
                parent.context,
                R.color.visit_workbook_column_header_title_text_color
            )

        val binding =
            ViewitemWorkbookMdlpBinding.inflate(parent.context.layoutInflater, parent, false)

        return ViewHolder(binding)
    }


    override fun onBindViewHolder(
        items: MutableList<Any>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: MutableList<Any>
    ) {
        dataSize = items.size
        (holder as ViewHolder).bind(
            items[position] as WorkBookView,
            position
        )
    }

    private inner class ViewHolder(
        private val binding: ViewitemWorkbookMdlpBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private val focusColor =
            ContextCompat.getColor(binding.frame.context, R.color.organization_unchecked_color)
        private val unFocusColor = ContextCompat.getColor(binding.frame.context, R.color.white)

        init {


            binding.expandButton.setOnCheckedChangeListener { view, isExpand ->
                if (isExpand) {
                    binding.commentFrame.visibility = View.VISIBLE
                    expandPositions.add(view.getTag(R.id.TAG_POSITION) as Int)
                } else {
                    binding.commentFrame.visibility = View.GONE
                    expandPositions.remove(view.getTag(R.id.TAG_POSITION) as Int)
                }
            }


            binding.fact.onFocusChangeListener =
                View.OnFocusChangeListener { v, hasFocus ->

                    if (!hasFocus && v.getTag(R.id.TAG_POSITION) != null && v.getTag(R.id.TAG_WB_ITEM) != null) {
                        val fact = (v as EditText).text.toString()
                        val wbItem =
                            v.getTag(R.id.TAG_WB_ITEM) as WorkBookView
                        //строка которую необходимо обновить после записи в БД
                        val rawPos = v.getTag(R.id.TAG_POSITION) as Int
                        //если значение не цифра - выходим
                        if (!isValidIntNum(fact)) return@OnFocusChangeListener
                        //записать в базу если изменилось значение
                        if (fact.toLong() != wbItem.purchaseAmount && canWriteToDB(wbItem)) {
                            doChange(wbItem, fact, TYPE_PURCH, rawPos)
                        }
                    }
                }


            binding.nextAgr.onFocusChangeListener =
                View.OnFocusChangeListener { v, hasFocus ->

                    if (!hasFocus && v.getTag(R.id.TAG_POSITION) != null && v.getTag(R.id.TAG_WB_ITEM) != null) {
                        val promise = (v as EditText).text.toString()
                        val wbItem =
                            v.getTag(R.id.TAG_WB_ITEM) as WorkBookView
                        //строка которую необходимо обновить после записи в БД
                        val rawPos = v.getTag(R.id.TAG_POSITION) as Int
                        //если значение не цифра - выходим
                        if (!isValidIntNum(promise)) return@OnFocusChangeListener
                        //блокировать создание дубля при быстром заполнении рабочей тертради
                        //записать в базу если изменилось значение
                        if (promise.toLong() != wbItem.promiseAmount && canWriteToDB(wbItem)) {
                            doChange(wbItem, promise, TYPE_PROMISE, rawPos)
                        }
                    }
                }

            binding.comment.onFocusChangeListener =
                View.OnFocusChangeListener { v, hasFocus ->

                    if (!hasFocus && v.getTag(R.id.TAG_POSITION) != null && v.getTag(R.id.TAG_WB_ITEM) != null) {
                        val comment = (v as EditText).text.toString()
                        val wbItem =
                            v.getTag(R.id.TAG_WB_ITEM) as WorkBookView
                        //строка которую необходимо обновить после записи в БД
                        val rawPos = v.getTag(R.id.TAG_POSITION) as Int
                        //блокировать создание дубля при быстром заполнении рабочей тертради
                        //записать в базу если изменилось значение
                        if (comment != wbItem.comment && canWriteToDB(wbItem)) {
                            doChange(wbItem, comment, TYPE_COMMENT, rawPos)
                        }
                    }
                }

            binding.remain.setOnEditorActionListener { v, actionId, _ ->
                return@setOnEditorActionListener if (actionId == EditorInfo.IME_ACTION_NEXT && v.getTag(
                        R.id.TAG_POSITION
                    ) != null
                ) {

                    //где
                    val raw = v.getTag(R.id.TAG_POSITION) as Int
                    //куда
                    val nextRaw = if (raw == (dataSize - 1)) 0 else raw + 1
                    //если нажата кнопка "Следующий" на последнем элементе в строке, то перевести фокус на первый элемент следующей строки
                    isFocusNeed = true
                    rawForFocus = nextRaw
                    val manager = recycler.layoutManager
                    manager?.scrollToPosition(nextRaw)
                    //обновить следующуб строку чтобы поставить фокус на первый editText
                    recycler.adapter?.notifyItemChanged(nextRaw)
                    true
                } else false
            }


            binding.remain.setOnFocusChangeListener { v, hasFocus ->

                if (!hasFocus && v.getTag(R.id.TAG_POSITION) != null && v.getTag(R.id.TAG_WB_ITEM) != null) {
                    val rem = (v as EditText).text.toString()
                    val wbItem =
                        v.getTag(R.id.TAG_WB_ITEM) as WorkBookView
                    //строка которую необходимо обновить после записи в БД
                    val rawPos = v.getTag(R.id.TAG_POSITION) as Int
                    //если значение не цифра - выходим
                    if (!isValidIntNum(rem)) return@setOnFocusChangeListener
                    //блокировать создание дубля при быстром заполнении рабочей тертради
                    //записать в базу если изменилось значение
                    if (rem.toLong() != wbItem.remainAmount && canWriteToDB(wbItem)) {
                        doChange(wbItem, rem, TYPE_REMAIN, rawPos)
                    }
                }
            }
        }

        fun bind(item: WorkBookView, pos: Int) {

            //присоединяем к каждому EditText null перед binding
            binding.fact.setTag(R.id.TAG_WB_ITEM, null)
            binding.nextAgr.setTag(R.id.TAG_WB_ITEM, null)
            binding.comment.setTag(R.id.TAG_WB_ITEM, null)

            binding.fact.setTag(R.id.TAG_POSITION, null)
            binding.nextAgr.setTag(R.id.TAG_POSITION, null)
            binding.comment.setTag(R.id.TAG_POSITION, null)

            binding.remain.setTag(R.id.TAG_POSITION, null)
            binding.remain.setTag(R.id.TAG_WB_ITEM, null)


            binding.expandButton.setTag(R.id.TAG_POSITION, pos)
            binding.expandButton.isChecked = expandPositions.contains(pos)


            val frameBackColor =
                if (item.focusSku == null || item.focusSku!! < 1.0) unFocusColor else focusColor
            binding.frame.setBackgroundColor(frameBackColor)

            //запрещаем редактирование если визит завершен

            binding.fact.isEnabled = !(item.isActComplete ?: true)
            binding.nextAgr.isEnabled = !(item.isActComplete ?: true)
            binding.comment.isEnabled = !(item.isActComplete ?: true)
            binding.remain.isEnabled = !(item.isActComplete ?: true)


            //обновляем тольлко тот View где были изменения

            val brandName = item.prodNameA ?: ""
            val skuName = item.prodNameB ?: ""
            binding.workbookBrandTextView.text = brandName
            binding.workbookSkuTextView.text = skuName


            //данные полученные через web или из локальной базы
            /*
            val planFact = if (item.isSoShow == true) {
                "${item.planSoQ ?: "-"}/${item.planSoM ?: "-"}/${item.factSoQ ?: "-"}/${item.utilMdlp ?: "-"}"
            } else {
                "${((item.quarterPlan ?: 0f) * 1000).toInt()}/${(item.monthPlan * 1000).toInt()}/${((item.quarterFact ?: 0f) * 1000).toInt()}/${item.utilMdlp ?: "-"}"
            }

             */

            val qPlanRounded = if (item.planS3Q != null) Math.round(item.planS3Q!!) else "-"
            val mPlanRounded = if (item.planS3M != null) Math.round(item.planS3M!!) else "-"
            val planFact = "$qPlanRounded/$mPlanRounded/${item.utilMdlp ?: "-"}"


            binding.workbookPlanTextView.text = planFact

            if (item.isS3Show == true && item.isS3DataLoadOk == false) {
                binding.workbookPlanTextView.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.red
                    )
                )
            } else {
                binding.workbookPlanTextView.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.gray
                    )
                )
            }


            val monthAmount = item.purchaseAmount?.toString() ?: ""
            binding.fact.setText(monthAmount)

            val promiseAmount = item.promiseAmount?.toString() ?: ""
            binding.nextAgr.setText(promiseAmount)

            val remainMdlp = item.remainAmount?.toString() ?: ""
            binding.remain.setText(remainMdlp)


            if (item.remainAmount != item.remainMdlp) binding.remain.setTextColor(
                ContextCompat.getColor(
                    binding.root.context,
                    R.color.black
                )
            ) else {
                binding.remain.setTextColor(
                    ContextCompat.getColor(
                        binding.root.context,
                        R.color.gray
                    )
                )
            }

            addCurrentComment(item)
            addHistoryComments(item)

            val prevAgr = item.prevAgreements?.toString() ?: ""
            binding.prevAgr.setText(prevAgr)


            //поставит фокус на первый editText в случае перехода на следующую строку
            if (isFocusNeed && rawForFocus == pos) {
                binding.fact.requestFocus()
                isFocusNeed = false
                rawForFocus = -1
            }

            //binding.available.isChecked = item.isAvailable

            //присоединяем к каждому EditText объект модели и позиццию в списке
            binding.fact.setTag(R.id.TAG_WB_ITEM, item)
            binding.nextAgr.setTag(R.id.TAG_WB_ITEM, item)
            binding.comment.setTag(R.id.TAG_WB_ITEM, item)
            binding.fact.setTag(R.id.TAG_POSITION, pos)
            binding.nextAgr.setTag(R.id.TAG_POSITION, pos)
            binding.comment.setTag(R.id.TAG_POSITION, pos)
            binding.remain.setTag(R.id.TAG_POSITION, pos)
            binding.remain.setTag(R.id.TAG_WB_ITEM, item)


            //sku записан
            if (item._ID != 0L) {
                skuForDb.remove(item.skuId)
                skuInDb.add(item.skuId)
            }

        }

        private fun addHistoryComments(item: WorkBookView) {


            if (item.skuComment?.isNotEmpty() == true) {
                val str = StringBuilder()
                item.skuComment?.forEach {
                    val dateOnly = it.onDate.substring(0, 10)
                    val text = "${dateOnly}: ${it.comment}\n"
                    str.append(text)
                }
                binding.prevComments.text = str.toString()
            } else binding.prevComments.text = ""
        }

        private fun addCurrentComment(item: WorkBookView) {

            if (item.comment != null) {
                binding.comment.setText(item.comment)
            } else {
                binding.comment.setText("")
                binding.comment.hint = "Комментариев нет"
            }
        }

        //возможна ли запись в базу данного элемента рабочей тетради
        private fun canWriteToDB(item: WorkBookView): Boolean {
            //содержится среди тех которые уже записываются но нет в записаных
            return if (skuForDb.contains(item.skuId) || (item._ID == 0L && skuInDb.contains(item.skuId))) false
            else {
                //для нового элемента добавить во множество записывемых
                if (item._ID == 0L) skuForDb.add(item.skuId)
                true
            }
        }
    }
}