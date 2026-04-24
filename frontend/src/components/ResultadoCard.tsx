import React from 'react';
import { motion } from 'framer-motion';
import { Car, Clock, ChevronRight } from 'lucide-react';

interface ResultadoCardProps {
  tipo: string;
  precio: string;
  tiempo: string;
  color: string;
  icon: React.ReactNode;
  delay: number;
  onClick?: () => void;
}

const ResultadoCard: React.FC<ResultadoCardProps> = ({ tipo, precio, tiempo, color, icon, delay, onClick }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5, delay }}
      whileHover={{ scale: 1.02 }}
      onClick={onClick}
      className="bg-white rounded-2xl p-5 shadow-sm border border-gray-100 flex items-center justify-between mb-4 cursor-pointer hover:shadow-md transition-shadow"
    >
      <div className="flex items-center gap-4">
        <div className={`p-3 rounded-xl ${color} text-white`}>
          {icon}
        </div>
        <div>
          <h3 className="font-bold text-gray-900 text-lg">{tipo}</h3>
          <div className="flex items-center text-gray-500 text-sm gap-1">
            <Clock size={14} />
            <span>{tiempo}</span>
          </div>
        </div>
      </div>
      
      <div className="text-right flex items-center gap-3">
        <div className="flex flex-col items-end">
          <span className="text-xl font-bold text-gray-900">{precio}</span>
          <button 
            onClick={(e) => {
              e.stopPropagation();
              onClick?.();
            }}
            className="text-sm font-semibold text-blue-600 hover:text-blue-700 flex items-center gap-1 mt-1"
          >
            Elegir <ChevronRight size={16} />
          </button>
        </div>
      </div>
    </motion.div>
  );
};

export default ResultadoCard;
