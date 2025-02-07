import { Line, LineChart, ResponsiveContainer, Tooltip } from "recharts";


type OverviewProps = {
  data: {
    date: string;
    revenue: number
  }[]
}

export function Overview(props: OverviewProps) {
  const {data} = props;
  
  return (
    <ResponsiveContainer width="100%" height={350}>
      <LineChart data={data}>
        <Tooltip
          content={({ active, payload }) => {
            if (active && payload && payload.length) {
              return (
                <div className="rounded-lg border bg-background p-2 shadow-sm">
                  <div className="grid grid-cols-2 gap-2">
                    <div className="flex flex-col">
                      <span className="text-[0.70rem] uppercase text-muted-foreground">
                        Revenue
                      </span>
                      <span className="font-bold text-muted-foreground">
                        ${payload[0].value}
                      </span>
                    </div>
                  </div>
                </div>
              )
            }
            return null
          }}
        />
        <Line
          type="monotone"
          dataKey="revenue"
          strokeWidth={2}
          activeDot={{
            r: 6,
            style: { fill: "hsl(var(--primary))", opacity: 0.8 },
          }}
          style={{
            stroke: "hsl(var(--primary))",
            opacity: 0.8,
          }}
        />
      </LineChart>
    </ResponsiveContainer>
  )
}