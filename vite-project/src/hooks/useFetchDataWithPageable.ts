import { useQuery } from "@tanstack/react-query";
import axios from 'axios';
import { useState } from "react";

export const useFetchDataWithPageable = ({table}: {table: string}) => {
    const [page, setPage] = useState(0);
    const [size, setSize] = useState(10);
    const [sort, setSort] = useState({ field: "id", direction: "asc" });

    const queryRes = useQuery({
        queryKey: [table, page, size, sort],
        queryFn: async () => {
            const response = await axios.get( `http://localhost:8080/api/${table}?page=${page}&size=${size}&sort=${sort.field},${sort.direction}`);
            return response.data;
        },
        staleTime: 5000,
        placeholderData: (previousData) => previousData
    });

    const {data} = queryRes
    const pageInfo = {
        totalElements: data?.data?.totalElements || 0,
        currentPage: page,
        totalPages: data?.data?.totalPages || 0
    };

    const handlePageChange = (newPage: number) => {
        setPage(newPage);
    };

    return {
        handlePageChange,
        setPage,
        setSize,
        setSort,
        pageInfo,
        queryRes
    }
}